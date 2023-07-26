package digital.slovensko.autogram.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";

    public Reports validate(SignedDocumentValidator docValidator) {
        var trustedListCertificateSource = buildLOTLCertificateSource();
        var verifier = new CommonCertificateVerifier();

        verifier.setTrustedCertSources(trustedListCertificateSource);
        verifier.setCrlSource(new OnlineCRLSource());
        verifier.setOcspSource(new OnlineOCSPSource());

        docValidator.setCertificateVerifier(verifier);
        var result = docValidator.validateDocument();

        saveResults(result);

        return result;
    }

    private TrustedListsCertificateSource buildLOTLCertificateSource() {
        var trustedListCertificateSource = new TrustedListsCertificateSource();
        var lotlSource = new LOTLSource();
        var offlineFileLoader = new FileCacheDataLoader();
        var validationJob = new TLValidationJob();

        lotlSource.setCertificateSource(getJournalCertificateSource());
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setPivotSupport(true);

        var targetLocation = Path.of(System.getProperty("user.dir"), "cache", "certs").toFile();
        targetLocation.mkdirs();
        offlineFileLoader.setCacheExpirationTime(21600000);
        offlineFileLoader.setDataLoader(new CommonsDataLoader());
        offlineFileLoader.setFileCacheDirectory(targetLocation);

        validationJob.setTrustedListCertificateSource(trustedListCertificateSource);
        validationJob.setListOfTrustedListSources(lotlSource);
        validationJob.setSynchronizationStrategy(new ExpirationAndSignatureCheckStrategy());
        validationJob.setOfflineDataLoader(offlineFileLoader);
        validationJob.setExecutorService(Executors.newSingleThreadExecutor());

        validationJob.offlineRefresh();

        return trustedListCertificateSource;
    }

    private CertificateSource getJournalCertificateSource() throws AssertionError {
        try {
            var keystore = new File("src/main/resources/lotlKeyStore.p12");
            return new KeyStoreCertificateSource(keystore, "PKCS12", "dss-password");

        } catch (IOException e) {
            throw new AssertionError("Cannot load LOTL keystore", e);
        }
    }

    private void saveResults(Reports r) {
        try {
            var writer = new FileWriter(new File("cache/reportDiag.xml"));
            writer.write(r.getXmlDiagnosticData());
            writer.close();
        } catch (IOException e) {
        }
        try {
            var writer = new FileWriter(new File("cache/reportSimple.xml"));
            writer.write(r.getXmlSimpleReport());
            writer.close();
        } catch (IOException e) {
        }
        try {
            var writer = new FileWriter(new File("cache/reportDetailed.xml"));
            writer.write(r.getXmlDetailedReport());
            writer.close();
        } catch (IOException e) {
        }
    }
}
