package digital.slovensko.autogram.core;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.exception.IllegalInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.dto.SignedDocumentSignature;
import digital.slovensko.autogram.util.XMLUtils;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.tsl.LOTLInfo;
import eu.europa.esig.dss.model.tsl.OtherTSLPointer;
import eu.europa.esig.dss.model.tsl.TLInfo;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.function.TLPredicateFactory;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

import static digital.slovensko.autogram.util.DSSUtils.*;

public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";
    /**
     * Loading waits for every list, a list that downloads for minutes (e.g. Belgium's in 10/2026) is left out. Slow but
     * steady connections still get the big ones, e.g. Spain's has 3 MB.
     */
    private static final Duration TRUSTED_LIST_ANSWER_LIMIT = Duration.ofSeconds(20);
    private static final long TRUSTED_LIST_MIN_BYTES_PER_SECOND = 20 * 1024;
    private CertificateVerifier verifier;
    private CertificateVerifier customVerifier;
    private TLValidationJob validationJob;
    private ExecutorService executorService;
    private final Object loadLock = new Object();
    private CompletableFuture<CertificateVerifier> loaded = new CompletableFuture<>();
    /** The load the running {@link #initialize} completes, a reload announced meanwhile needs a new one. */
    private CompletableFuture<CertificateVerifier> loading;
    /** Read without the lock, which loading and validation hold while they wait for slow servers. */
    private volatile boolean tlsLoaded;
    /** Read without the lock like {@link #tlsLoaded}. */
    private volatile FailedTLs failedTLs = FailedTLs.NONE;
    private static Logger logger = LoggerFactory.getLogger(SignatureValidator.class);

    /**
     * Trusted lists of the selected countries that are not used: not downloaded in time, broken or expired.
     * @param countries their territories as in the trusted lists, e.g. EL for Greece
     */
    public record FailedTLs(int failed, int total, Set<String> countries) {
        static final FailedTLs NONE = new FailedTLs(0, 0, Set.of());
    }

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

        // TODO: do not print stack trace inside DSS
        return docValidator.validateDocument();
    }

    /**
     * Validates with the given verifier (its trusted sources and revocation sources) instead of the EU trusted lists,
     * which are then never downloaded. For tests and the test harness only, deliberately not exposed as a setting.
     * Pass null to go back to the trusted lists with the next {@link #initialize}.
     */
    public synchronized void useCustomVerifier(CertificateVerifier verifier) {
        customVerifier = verifier;
        this.verifier = verifier;
        validationJob = null;
        tlsLoaded = verifier != null;
        failedTLs = FailedTLs.NONE;
        if (verifier == null)
            reloadStarting();
        else
            startLoading().complete(verifier);
    }

    /**
     * Completes with the verifier once the trusted lists are loaded. After {@link #reloadStarting} it is a new one,
     * completed by the next {@link #initialize} or {@link #updateLotl}.
     */
    public CompletableFuture<CertificateVerifier> whenLoaded() {
        synchronized (loadLock) {
            return loaded;
        }
    }

    /** Call before the lists are reloaded on another thread, so that checks started in the meantime wait for them. */
    public void reloadStarting() {
        synchronized (loadLock) {
            if (loaded.isDone() || loaded == loading)
                loaded = new CompletableFuture<>();
        }
    }

    private CompletableFuture<CertificateVerifier> startLoading() {
        synchronized (loadLock) {
            if (loaded.isDone())
                loaded = new CompletableFuture<>();
            loading = loaded;
            return loading;
        }
    }

    public synchronized void updateLotl(List<String> tlCountries) {
        initialize(executorService, tlCountries);
    }

    public synchronized void refresh() {
        if (customVerifier != null)
            return;

        offlineRefresh();
    }

    public synchronized void initialize(ExecutorService executorService, List<String> tlCountries) {
        var load = startLoading();
        try {
            loadTrustedLists(executorService, tlCountries);
        } finally {
            load.complete(verifier);
        }
    }

    private void loadTrustedLists(ExecutorService executorService, List<String> tlCountries) {
        this.executorService = executorService;
        if (customVerifier != null) {
            verifier = customVerifier;
            logger.debug("Signature validator uses a custom verifier, trusted lists are not loaded");
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        logger.debug("Initializing signature validator at {}", formatter.format(new Date()));

        validationJob = new TLValidationJob();

        var lotlSource = new LOTLSource();
        lotlSource.setCertificateSource(getJournalCertificateSource());
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setPivotSupport(true);
        lotlSource.setTlPredicate(TLPredicateFactory.createEUTLCountryCodePredicate(tlCountries.toArray(new String[0])));

        var offlineFileLoader = NetworkClients.fileCacheDataLoader(21600000, TRUSTED_LIST_ANSWER_LIMIT,
                TRUSTED_LIST_MIN_BYTES_PER_SECOND);
        validationJob.setOfflineDataLoader(offlineFileLoader);

        var onlineFileLoader = NetworkClients.fileCacheDataLoader(0, TRUSTED_LIST_ANSWER_LIMIT,
                TRUSTED_LIST_MIN_BYTES_PER_SECOND);
        validationJob.setOnlineDataLoader(onlineFileLoader);

        var trustedListCertificateSource = new TrustedListsCertificateSource();
        validationJob.setTrustedListCertificateSource(trustedListCertificateSource);
        validationJob.setListOfTrustedListSources(lotlSource);
        validationJob.setSynchronizationStrategy(new CustomSynchronizationStrategy());
        validationJob.setExecutorService(executorService);
        validationJob.setDebug(false);

        logger.debug("Starting signature validator offline refresh");
        offlineRefresh();

        verifier = NetworkClients.certificateVerifier();
        verifier.setTrustedCertSources(trustedListCertificateSource);
        verifier.setCrlSource(new OnlineCRLSource(NetworkClients.dataLoader()));
        verifier.setOcspSource(new OnlineOCSPSource(NetworkClients.ocspDataLoader()));

        logger.debug("Signature validator initialized at {}", formatter.format(new Date()));
    }

    private CertificateSource getJournalCertificateSource() throws AssertionError {
        try {
            var keystore = getClass().getResourceAsStream("lotlKeyStore.p12");
            return new KeyStoreCertificateSource(keystore, "PKCS12", "dss-password".toCharArray());

        } catch (DSSException | NullPointerException e) {
            throw new AssertionError("Cannot load LOTL keystore", e);
        }
    }

    public synchronized ValidationReports getSignatureValidationReport(SigningJob job) {
        var documentReports = new ArrayList<ValidationReports.DocumentReport>();
        var documents = job.getDocuments();

        for (int index = 0; index < documents.size(); index++) {
            var document = documents.get(index).toDssDocument();
            var documentValidator = createDocumentValidator(document);
            if (documentValidator == null)
                continue;

            var reports = validate(documentValidator);
            if (!hasSignatures(reports))
                continue;

            documentReports.add(new ValidationReports.DocumentReport(index, document, reports));
        }

        return new ValidationReports(documentReports, job);
    }

    public static String getSignatureValidationReportHTML(Reports signatureValidationReport) {
        return wrapSignatureValidationReportHTML(getSignatureValidationReportBodyHTML(signatureValidationReport));
    }

    public static String getSignatureValidationReportBodyHTML(Reports signatureValidationReport) {
        try {
            var document = XMLUtils.getSecureDocumentBuilder().parse(new InputSource(new StringReader(signatureValidationReport.getXmlSimpleReport())));
            var xmlSource = new DOMSource(document);

            var xsltFile = SignatureValidator.class.getResourceAsStream("simple-report-bootstrap4.xslt");
            var xsltSource = new StreamSource(xsltFile);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = XMLUtils.getSecureTransformerFactory().newTransformer(xsltSource);
            transformer.transform(xmlSource, outputTarget);

            return outputTarget.getWriter().toString().trim();

        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            return "Error transforming validation report";
        }
    }

    public static String wrapSignatureValidationReportHTML(String content) {
        try {
            var templateFile = SignatureValidator.class.getResourceAsStream("simple-report-template.html");
            var templateString = new String(templateFile.readAllBytes(), StandardCharsets.UTF_8);
            return templateString.replace("{{content}}", content);
        } catch (IOException | NullPointerException e) {
            return content;
        }
    }

    // Returns a report indicating which documents in the signing job have signatures.
    // Using empty CertificateVerifier so it does not perform any actual certificate validation,
    // just fast structure and integrity check.
    public static ValidationReports getSignatureCheckReport(SigningJob job) {
        var documentReports = new ArrayList<ValidationReports.DocumentReport>();
        var documents = job.getDocuments();

        for (int index = 0; index < documents.size(); index++) {
            var document = documents.get(index).toDssDocument();
            var validator = createDocumentValidator(document);
            if (validator == null)
                continue;

            validator.setCertificateVerifier(NetworkClients.certificateVerifier());
            var reports = validator.validateDocument();
            if (hasSignatures(reports))
                documentReports.add(new ValidationReports.DocumentReport(index, document, reports));
        }

        return new ValidationReports(documentReports, job);
    }

    public static SimpleReport getSignedDocumentSimpleReport(DSSDocument document) {
        var validator = createDocumentValidator(document);
        if (validator == null)
            return null;

        validator.setCertificateVerifier(NetworkClients.certificateVerifier());
        var report = validator.validateDocument().getSimpleReport();
        if (report.getSignatureIdList().size() == 0)
            return null;

        return report;
    }

    public static SignedDocumentSignature getSignedDocumentSignature(DSSDocument document) {
        SimpleReport report;
        try {
            report = getSignedDocumentSimpleReport(document);
        } catch (IllegalInputException e) {
            return null;
        }

        if (report == null)
            return null;

        var level = report.getSignatureFormat(report.getSignatureIdList().get(0));
        if (level == null)
            return null;

        return new SignedDocumentSignature(level.getSignatureForm(), report.getContainerType(),
                getSignaturePackaging(document, level, report.getContainerType()));
    }

    private static SignaturePackaging getSignaturePackaging(DSSDocument document, SignatureLevel level,
            ASiCContainerType container) {
        if (container != null || level.getSignatureForm() == SignatureForm.PAdES)
            return null;

        if (level.getSignatureForm() == SignatureForm.CAdES)
            return SignaturePackaging.ENVELOPING;

        if (level.getSignatureForm() != SignatureForm.XAdES)
            return null;

        try (var inputStream = document.openStream()) {
            var documentElement = XMLUtils.getSecureDocumentBuilder().parse(inputStream).getDocumentElement();
            if ("Signature".equals(documentElement.getLocalName())
                    && "http://www.w3.org/2000/09/xmldsig#".equals(documentElement.getNamespaceURI()))
                return SignaturePackaging.ENVELOPING;

            return SignaturePackaging.ENVELOPED;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new IllegalArgumentException("Unable to determine XAdES signature packaging", e);
        }
    }

    private void offlineRefresh() {
        validationJob.offlineRefresh();
        // the same check that decides which lists the job puts into the trusted certificate source
        var strategy = new CustomSynchronizationStrategy();
        int total = 0, failed = 0;
        var countries = new HashSet<String>();
        for (var lotl : validationJob.getSummary().getLOTLInfos()) {
            for (var list : lotl.getTLInfos()) {
                total++;
                if (!strategy.canBeSynchronized(list)) {
                    failed++;
                    countries.add(territoryOf(list, lotl));
                }
            }
        }
        failedTLs = new FailedTLs(failed, total, Set.copyOf(countries));
        tlsLoaded = failed < total;
    }

    /** A list that was not downloaded has no territory of its own, the LOTL's pointer to it has. */
    private static String territoryOf(TLInfo list, LOTLInfo lotl) {
        var pointers = lotl.getParsingCacheInfo().getTlOtherPointers();
        return Optional.ofNullable(pointers).orElse(List.of()).stream()
                .filter(pointer -> list.getUrl().equals(pointer.getTSLLocation()))
                .map(OtherTSLPointer::getSchemeTerritory)
                .filter(Objects::nonNull)
                .findFirst()
                .or(() -> Optional.ofNullable(list.getParsingCacheInfo().getTerritory()))
                .orElse(list.getUrl());
    }

    /** At least one trusted list is used. */
    public boolean areTLsLoaded() {
        // TODO: consider validation turned off as well
        return tlsLoaded;
    }

    public FailedTLs getFailedTLs() {
        return failedTLs;
    }

    private static boolean hasSignatures(Reports reports) {
        return reports != null && reports.getSimpleReport().getSignaturesCount() > 0;
    }

}
