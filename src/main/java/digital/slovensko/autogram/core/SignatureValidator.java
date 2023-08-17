package digital.slovensko.autogram.core;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.function.TLPredicateFactory;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

import static digital.slovensko.autogram.util.DSSUtils.*;

public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";
    private CertificateVerifier verifier;
    private TLValidationJob validationJob;
    private static Logger logger = LoggerFactory.getLogger(SignatureValidator.class);

    // Singleton
    private static SignatureValidator instance;

    private SignatureValidator() {
    }

    public synchronized static SignatureValidator getInstance() {
        if (instance == null)
            instance = new SignatureValidator();

        return instance;
    }

    public synchronized Reports validate(SignedDocumentValidator docValidator) {
        docValidator.setCertificateVerifier(verifier);
        return docValidator.validateDocument();
    }

    public synchronized void refresh() {
        System.out.println("Refreshing signature validator...");
        validationJob.offlineRefresh();
        System.out.println("Signature validator refreshed");
    }

    public synchronized void initialize() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        logger.debug("Initializing signature validator at {}", formatter.format(new Date()));

        validationJob = new TLValidationJob();

        var lotlSource = new LOTLSource();
        lotlSource.setCertificateSource(getJournalCertificateSource());
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setPivotSupport(true);
        lotlSource.setTlPredicate(TLPredicateFactory.createEUTLCountryCodePredicate("SK", "CZ", "AT", "HU", "PL"));

        var targetLocation = Path.of(System.getProperty("user.dir"), "cache", "certs").toFile();
        targetLocation.mkdirs();

        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(21600000);
        offlineFileLoader.setDataLoader(new CommonsDataLoader());
        offlineFileLoader.setFileCacheDirectory(targetLocation);
        validationJob.setOfflineDataLoader(offlineFileLoader);

        var onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime(0);
        onlineFileLoader.setDataLoader(new CommonsDataLoader());
        onlineFileLoader.setFileCacheDirectory(targetLocation);
        validationJob.setOnlineDataLoader(onlineFileLoader);

        var trustedListCertificateSource = new TrustedListsCertificateSource();
        validationJob.setTrustedListCertificateSource(trustedListCertificateSource);
        validationJob.setListOfTrustedListSources(lotlSource);
        validationJob.setSynchronizationStrategy(new ExpirationAndSignatureCheckStrategy());
        validationJob.setExecutorService(Executors.newFixedThreadPool(4));
        validationJob.setDebug(true);

        logger.debug("Starting signature validator offline refresh");
        validationJob.offlineRefresh();

        verifier = new CommonCertificateVerifier();
        verifier.setTrustedCertSources(trustedListCertificateSource);
        verifier.setCrlSource(new OnlineCRLSource());
        verifier.setOcspSource(new OnlineOCSPSource());

        logger.debug("Signature validator initialized at {}", formatter.format(new Date()));
    }

    private CertificateSource getJournalCertificateSource() throws AssertionError {
        try {
            var keystore = getClass().getResourceAsStream("lotlKeyStore.p12");
            return new KeyStoreCertificateSource(keystore, "PKCS12", "dss-password");

        } catch (DSSException | NullPointerException e) {
            throw new AssertionError("Cannot load LOTL keystore", e);
        }
    }

    public synchronized Reports getSignatureValidationReport(DSSDocument document) {
        return validate(createDocumentValidator(document));
    }

    public static String getSignatureValidationReportHTML(Reports signatureValidationReport) {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        try {
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(signatureValidationReport.getXmlSimpleReport())));
            var xmlSource = new DOMSource(document);

            var xsltFile = SignatureValidator.class.getResourceAsStream("simple-report-bootstrap4.xslt");
            var xsltSource = new StreamSource(xsltFile);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
            transformer.transform(xmlSource, outputTarget);

            var r = outputTarget.getWriter().toString().trim();

            var templateFile = SignatureValidator.class.getResourceAsStream("simple-report-template.html");
            var templateString = new String(templateFile.readAllBytes());
            return templateString.replace("{{content}}", r);

        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            return "Error transforming validation report";
        }
    }

    public static Reports getSignatureCheckReport(DSSDocument document) {
        var validator = createDocumentValidator(document);
        if (validator == null)
            return null;

        validator.setCertificateVerifier(new CommonCertificateVerifier());
        return validator.validateDocument();
    }

    public static boolean hasSignatures(DSSDocument document) {
        var signatureCheckReport = getSignatureCheckReport(document);
        return signatureCheckReport != null && signatureCheckReport.getSimpleReport().getSignaturesCount() > 0;
    }
}
