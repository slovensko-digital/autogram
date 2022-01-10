package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.SelectableItem;
import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.validation.*;
import eu.europa.esig.dss.validation.reports.CertificateReports;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.text.SimpleDateFormat;

import static java.util.Objects.requireNonNull;

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

    public boolean isValid() {
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        DefaultAIASource aiaSource = new DefaultAIASource();
        aiaSource.setDataLoader(new CommonsDataLoader());
        commonCertificateVerifier.setAIASource(aiaSource);

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();

        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl("https://ec.europa.eu/tools/lotl/eu-lotl.xml");
        lotlSource.setPivotSupport(true);

        TrustedListsCertificateSource tslCertificateSource = new TrustedListsCertificateSource();

        FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader(commonsHttpDataLoader);

        CacheCleaner cacheCleaner = new CacheCleaner();
        cacheCleaner.setCleanFileSystem(true);
        cacheCleaner.setDSSFileLoader(onlineFileLoader);

        TLValidationJob validationJob = new TLValidationJob();
        validationJob.setTrustedListCertificateSource(tslCertificateSource);
        validationJob.setOnlineDataLoader(onlineFileLoader);
        validationJob.setCacheCleaner(cacheCleaner);
        validationJob.setListOfTrustedListSources(lotlSource);
        validationJob.onlineRefresh();

        commonCertificateVerifier.setTrustedCertSources(tslCertificateSource);

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(ocspDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);

        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        CertificateToken token = dssPrivateKey.getCertificate();
        CertificateValidator validator = CertificateValidator.fromCertificate(token);
        validator.setCertificateVerifier(commonCertificateVerifier);
        validator.setTokenExtractionStrategy(TokenExtractionStrategy.EXTRACT_CERTIFICATES_AND_REVOCATION_DATA);
        CertificateReports certificateReports = validator.validate();
        DiagnosticData diagnosticData = certificateReports.getDiagnosticData();

        return diagnosticData.isValidCertificate(token.getDSSIdAsString());
    }
}
