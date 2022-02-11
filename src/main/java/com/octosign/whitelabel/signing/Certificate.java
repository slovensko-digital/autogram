package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.signing.token.Token;
import com.octosign.whitelabel.ui.picker.SelectableItem;
import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.*;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.spi.client.http.*;
import eu.europa.esig.dss.spi.x509.*;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.validation.*;
import eu.europa.esig.dss.validation.reports.CertificateReports;

import javax.naming.ldap.*;
import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;
import static java.util.Objects.*;

/**
 * Represents a combination of Token and PrivateKey within that token
 */
public class Certificate implements SelectableItem {
    /**
     * Token set by technology-specific class (MSCAPI/PKCS11/PKCS12)
     */
    private final Token token;

    private final DSSPrivateKeyEntry dssPrivateKey;

    public Certificate(DSSPrivateKeyEntry dssPrivateKey, Token token) {
        this.dssPrivateKey = requireNonNull(dssPrivateKey);
        this.token = requireNonNull(token);
    }

    protected DSSPrivateKeyEntry getDssPrivateKey() {
        return dssPrivateKey;
    }

    protected Token getToken() {
        return token;
    }

    /**
     * How verbose should key description be
     * - LONG - Contains name, address, and date range
     * - SHORT - Contains name and date range
     * - NAME - Contains name only
     */
    public enum DescriptionVerbosity {
        LONG,
        SHORT,
        COMPACT,
        NAME
    }

    /**
     * Constructs human readable private key description
     */
    public static String getCertificateDescription(Certificate certificate, DescriptionVerbosity verbosity) {
        var privateKey = certificate.getDssPrivateKey();
        String dn = privateKey.getCertificate().getSubject().getRFC2253();
        String label = "";
        try {
            LdapName ldapDN = new LdapName(dn);
            String dnName = "";
            String dnCountry = "";
            String dnCity = "";
            String dnStreet = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String notBefore = dateFormat.format(privateKey.getCertificate().getNotBefore());
            String notAfter = dateFormat.format(privateKey.getCertificate().getNotAfter());
            for (Rdn rdn: ldapDN.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN"))
                    dnName = rdn.getValue().toString();
                if (rdn.getType().equalsIgnoreCase("C"))
                    dnCountry = rdn.getValue().toString();
                if (rdn.getType().equalsIgnoreCase("L"))
                    dnCity = rdn.getValue().toString();
                if (rdn.getType().equalsIgnoreCase("STREET"))
                    dnStreet = rdn.getValue().toString();
            }

            if (verbosity == DescriptionVerbosity.LONG) {
                label = String.format("%s, %s %s, %s (%s - %s)", dnName, dnCity, dnStreet, dnCountry, notBefore,
                    notAfter);
            } else if (verbosity == DescriptionVerbosity.SHORT) {
                label = String.format("%s (%s - %s)", dnName, notBefore, notAfter);
            } else if (verbosity == DescriptionVerbosity.COMPACT) {
                label = dnName;
            } else {
                label = dnName;
            }
        } catch (Exception e) {
            // If retrieving sensible name fails, use serial number
            label = "SN: " + privateKey.getCertificate().getCertificate().getSerialNumber().toString(16);
        }

        return label;
    }

    /**
     * Constructs human readable description for the current private key
     */
    public String getCertificateDescription(DescriptionVerbosity verbosity) {
        return getCertificateDescription(this, verbosity);
    }

    @Override
    public String getDisplayedName() {
        return this.getCertificateDescription(DescriptionVerbosity.NAME);
    }

    @Override
    public String getDisplayedDetails() {
        return this.getCertificateDescription(DescriptionVerbosity.COMPACT);
    }

    // temp benchmarking vars
    Long startTime;
    Map<String, Long> times = new LinkedHashMap<>();

    private void log(String label) {
        long endTime = System.currentTimeMillis();
        times.put(label, endTime - startTime);
        startTime = System.currentTimeMillis();
    }

    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";

    public boolean isValid() {
        startTime = System.currentTimeMillis();

        TLValidationJob validationJob = new TLValidationJob();
        validationJob.setListOfTrustedListSources(getLOTL());
        log("fetching LOTL");

        validationJob.setSynchronizationStrategy(new ExpirationAndSignatureCheckStrategy());
        validationJob.setTrustedListSources();
        validationJob.setOfflineDataLoader(offlineLoader());
        validationJob.setOnlineDataLoader(onlineLoader());
        validationJob.setDebug(true);
        validationJob.onlineRefresh();
//        validationJob.offlineRefresh();


        var summary = validationJob.getSummary();
        var lotlN = summary.getNumberOfProcessedLOTLs();
        var tlN = summary.getNumberOfProcessedTLs();
        System.out.println("\\\\\\\\\\\\\\\\////////////////\\\\\\\\\\\\\\\\/////////////////\\\\\\\\\\\\\\//////////////");
        System.out.printf("LOTLs count: %d \t\t TLs count: %d", lotlN, tlN);
        System.out.println("\\\\\\\\\\\\\\\\////////////////\\\\\\\\\\\\\\\\/////////////////\\\\\\\\\\\\\\//////////////");
//        summary.getLOTLInfos().forEach(System.out::println);
//        summary.getOtherTLInfos().forEach(System.out::println);
        log("LOTL other settings and feches");
//        validationJob.setExecutorService(Executors.newSingleThreadExecutor());


//        onlineFileLoader.setFileCacheDirectory();
//        CacheCleaner cacheCleaner = new CacheCleaner();
//        cacheCleaner.setCleanFileSystem(true);
//        cacheCleaner.setDSSFileLoader(onlineFileLoader);

        log("settings in between");

        DefaultAIASource aiaSource = new DefaultAIASource();
        aiaSource.setDataLoader(dataLoader());
        log("AIA source");

        OnlineCRLSource crlSource = new OnlineCRLSource();
        crlSource.setDataLoader(dataLoader());
        log("CRL source");

        OnlineOCSPSource ocspSource = new OnlineOCSPSource();
        ocspSource.setDataLoader(dataLoader());
        log("OCS source");

        CertificateVerifier verifier = new CommonCertificateVerifier();

        // THIS tooks way too much longer when uncommented
        // hopefully this is redundant check? try to find out
        //        verifier.setAIASource(aiaSource);

        verifier.setCrlSource(crlSource);
        verifier.setOcspSource(ocspSource);
        log("settings");

        CertificateValidator validator = CertificateValidator.fromCertificate(dssPrivateKey.getCertificate());
        validator.setCertificateVerifier(verifier);
        validator.setTokenExtractionStrategy(TokenExtractionStrategy.EXTRACT_CERTIFICATES_AND_REVOCATION_DATA);
        log("some settings");

        CertificateReports certificateReports = validator.validate();
        log("validator.validate");

        DiagnosticData diagnosticData = certificateReports.getDiagnosticData();
        DetailedReport detailedReport = certificateReports.getDetailedReport();
        SimpleCertificateReport simpleReport = certificateReports.getSimpleReport();
        log("reporting");


        System.out.println("===========================================");
        System.out.println("====    benchmarking results           ====");
        System.out.println("===========================================");
        times.forEach((k, v) -> System.out.printf("%s took %d milliseconds \n", k, v));
        System.out.println("===========================================");

        return true;
    }

    private LOTLSource getLOTL() {
        CertificateSource journalCertificateSource;

        Path keystorePath = Path.of(System.getProperty("user.dir"), getProperty("file.pkcs12.lotl.certs"));
        var password = getProperty("password.file.pkcs12.lotl.certs");

        try {
            var file = new FileInputStream(keystorePath.toFile());
            journalCertificateSource = new KeyStoreCertificateSource(file, "PKCS12", password);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setCertificateSource(journalCertificateSource);
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setPivotSupport(true);
        return lotlSource;
    }

    public DSSFileLoader offlineLoader() {
        FileCacheDataLoader offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(Long.MAX_VALUE);
        offlineFileLoader.setDataLoader(new IgnoreDataLoader());
        offlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
        return offlineFileLoader;
    }

    public DSSFileLoader onlineLoader() {
        FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime(0);
        onlineFileLoader.setDataLoader(dataLoader());
        onlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
        return onlineFileLoader;
    }

    private File tlCacheDirectory() {
         File targetLocation  = Path.of(System.getProperty("user.dir"), "cache", "certs").toFile();
         targetLocation.mkdirs();
         return targetLocation;
    }

    private CommonsDataLoader dataLoader() {
        return new CommonsDataLoader();
    }
}
