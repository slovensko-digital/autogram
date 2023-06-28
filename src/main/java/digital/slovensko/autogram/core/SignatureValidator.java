package digital.slovensko.autogram.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.source.TLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.validation.reports.Reports;

public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String TL_SK = "http://tl.nbu.gov.sk/kca/tsl/tsl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";
    private CertificateSource journalCertificateSource;
    private FileCacheDataLoader dataLoader;

    public Reports validate(DSSDocument doc, SignedDocumentValidator docValidator) {
        var trustedListCertificateSource = new TrustedListsCertificateSource();
        processLOTL(trustedListCertificateSource);

        var verifier = new CommonCertificateVerifier();
        verifier.setTrustedCertSources(trustedListCertificateSource);
        verifier.setAIASource(new DefaultAIASource(dataLoader));
        verifier.setCrlSource(new OnlineCRLSource(dataLoader));
        verifier.setOcspSource(new OnlineOCSPSource(dataLoader));

        docValidator.setCertificateVerifier(new CommonCertificateVerifier());
        docValidator.setCertificateVerifier(verifier);
        var r = docValidator.validateDocument();

        System.out.println("Done");
        saveResults(r);

        return r;
    }

    private void processLOTL(TrustedListsCertificateSource trustedListCertificateSource) {
        try {
            journalCertificateSource = new KeyStoreCertificateSource(
                new File("/home/turtle/slovensko_digital/autogram/src/main/resources/keyStore.p12"), "PKCS12",
                    "dss-password");
                } catch (IOException e) {
            throw new AssertionError(e);
        }

        File targetLocation = Path.of(System.getProperty("user.dir"), "cache", "certs").toFile();
        targetLocation.mkdirs();

        dataLoader = new FileCacheDataLoader();
        dataLoader.setDataLoader(new CommonsDataLoader());
        dataLoader.setCacheExpirationTime(-1);
        dataLoader.setFileCacheDirectory(targetLocation);

        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(3600000);
        offlineFileLoader.setDataLoader(dataLoader);
        offlineFileLoader.setFileCacheDirectory(targetLocation);

        var validationJob = new TLValidationJob();
        validationJob.setTrustedListCertificateSource(trustedListCertificateSource);

        var lotlSource = new LOTLSource();
        lotlSource.setCertificateSource(journalCertificateSource);
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setPivotSupport(true);
        validationJob.setListOfTrustedListSources(lotlSource);

        // var tlSource = new TLSource();
        // tlSource.setUrl(TL_SK);
        // tlSource.setCertificateSource(journalCertificateSource);
        // validationJob.setTrustedListSources(tlSource);

        // validationJob.setSynchronizationStrategy(new ExpirationAndSignatureCheckStrategy());
        validationJob.setSynchronizationStrategy(new AcceptAllStrategy());
        validationJob.setDebug(false);

        validationJob.setOfflineDataLoader(offlineFileLoader);
        validationJob.offlineRefresh();

    }

    private void saveResults(CertificateReports certificateReports) {
        try {
            var writer = new FileWriter(new File("/home/turtle/slovensko_digital/autogram/reportDiag.xml"));
            writer.write(certificateReports.getXmlDiagnosticData());
            writer.close();
        } catch (IOException e) {
        }
        try {
            var writer = new FileWriter(new File("/home/turtle/slovensko_digital/autogram/reportSimple.xml"));
            writer.write(certificateReports.getXmlSimpleReport());
            writer.close();
        } catch (IOException e) {
        }
        try {
            var writer = new FileWriter(new File("/home/turtle/slovensko_digital/autogram/reportDetailed.xml"));
            writer.write(certificateReports.getXmlDetailedReport());
            writer.close();
        } catch (IOException e) {
        }
    }

    private void printResults(CertificateReports reps) {
        System.out.println("-----------------");

        System.out.println("Qualification at issuance type " + reps.getSimpleReport().getQualificationAtCertificateIssuance().getType());
        System.out.println("Qualification at validation type " + reps.getSimpleReport().getQualificationAtValidationTime().getType());
        System.out.println("Is Qualifie Certificate " + reps.getSimpleReport().getQualificationAtValidationTime().isQc());
        System.out.println("Is QSC Device " + reps.getSimpleReport().getQualificationAtValidationTime().isQscd());
        System.out.println("Qualification name " + reps.getSimpleReport().getQualificationAtValidationTime().name());
        System.out.println("Qualification name " + reps.getSimpleReport().getQualificationAtValidationTime().toString());
        System.out.println("Qualification label " + reps.getSimpleReport().getQualificationAtValidationTime().getLabel());

        System.out.println("Indications");
        for (var cert : reps.getSimpleReport().getCertificateIds()) {
            System.out.println(reps.getSimpleReport().getCertificateCommonName(cert) + " "
                    + reps.getSimpleReport().getCertificateIndication(cert));
        }

        System.out.println("-----------------");
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
