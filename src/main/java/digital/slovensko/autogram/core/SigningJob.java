package digital.slovensko.autogram.core;

import java.io.File;
import java.io.IOException;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.cades.validation.ASiCContainerWithCAdESValidatorFactory;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.asic.xades.validation.ASiCContainerWithXAdESValidatorFactory;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidatorFactory;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.xades.signature.XAdESService;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidatorFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;
    private Reports signatureCheckReport;
    private Reports signatureValidationReport;
    private final MimeType transformationOutputMimeTypeForXdc;

    public SigningJob(CommonDocument document, SigningParameters parameters, Responder responder,
            MimeType transformationOutputMimeTypeForXdc) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
        this.transformationOutputMimeTypeForXdc = transformationOutputMimeTypeForXdc;
    }

    public SigningJob(CommonDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
        this.transformationOutputMimeTypeForXdc = null;
    }

    public CommonDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    public int getVisualizationWidth() {
        return parameters.getVisualizationWidth();
    }

    private boolean isDocumentXDC() {
        return document.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER);
    }

    public void signWithKeyAndRespond(SigningKey key) throws InterruptedException {

        Logging.log("Signing Job: " + this.hashCode() + " file " + getDocument().getName());
        boolean isContainer = getParameters().getContainer() != null;
        var doc = switch (getParameters().getSignatureType()) {
            case XAdES -> isContainer ? signDocumentAsAsiCWithXAdeS(key) : signDocumentAsXAdeS(key);
            case CAdES -> isContainer ? signDocumentAsASiCWithCAdeS(key) : signDocumentAsCAdeS(key);
            case PAdES -> signDocumentAsPAdeS(key);
            default -> throw new RuntimeException(
                    "Unsupported signature type: " + getParameters().getSignatureType());
        };
        responder.onDocumentSigned(new SignedDocument(doc, key.getCertificate()));
    }

    public void onDocumentSignFailed(AutogramException e) {
        responder.onDocumentSignFailed(e);
    }

    private DSSDocument signDocumentAsCAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new CAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getCAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningKey key) {
        DSSDocument doc = getDocument();
        if (getParameters().shouldCreateDatacontainer() && !isDocumentXDC()) {
            var transformer = XDCTransformer.buildFromSigningParameters(getParameters(),
                    transformationOutputMimeTypeForXdc);
            doc = transformer.transform(doc);
            doc.setMimeType(AutogramMimeType.XML_DATACONTAINER);
        }

        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithXAdESService(commonCertificateVerifier);
        var signatureParameters = getParameters().getASiCWithXAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(doc, signatureParameters);
        var signatureValue = key.sign(dataToSign, getParameters().getDigestAlgorithm());

        return service.signDocument(doc, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsXAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new XAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getXAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsASiCWithCAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithCAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getASiCWithCAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsPAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new PAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getPAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    public static SigningJob buildFromFile(File file, Responder responder, boolean checkPDFACompliance) {
        var document = new FileDocument(file);
        SigningParameters parameters = getParametersForFile(file, checkPDFACompliance);
        return new SigningJob(document, parameters, responder);
    }

    public static SigningJob buildFromFileBatch(File file, Autogram autogram, Responder responder) {
        var document = new FileDocument(file);
        SigningParameters parameters = getParametersForFile(file, false);
        return new SigningJob(document, parameters, responder);
    }

    private static SigningParameters getParametersForFile(File file, boolean checkPDFACompliance) {
        var filename = file.getName();

        if (filename.endsWith(".pdf")) {
            return SigningParameters.buildForPDF(filename, checkPDFACompliance);
        } else {
            return SigningParameters.buildForASiCWithXAdES(filename);
        }

    }

    public boolean shouldCheckPDFCompliance() {
        return parameters.getCheckPDFACompliance();
    }

    public SignedDocumentValidator getDocumentValidator() {
        if (new ASiCContainerWithXAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithXAdESValidatorFactory().create(document);

        if (new ASiCContainerWithCAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithCAdESValidatorFactory().create(document);

        if (new PDFDocumentValidatorFactory().isSupported(document))
            return new PDFDocumentValidatorFactory().create(document);

        if (new XMLDocumentValidatorFactory().isSupported(document))
            return new XMLDocumentValidatorFactory().create(document);

        return null;
    }

    public void checkForSignatures() {
        var validator = getDocumentValidator();
        if (validator == null)
            return;

        validator.setCertificateVerifier(new CommonCertificateVerifier());
        signatureCheckReport = validator.validateDocument();
    }

    public Reports getSignatureCheckReport() {
        return signatureCheckReport;
    }

    public void validateSignatures() {
        var signatureValidator = SignatureValidator.getInstance();
        signatureValidationReport = signatureValidator.validate(getDocumentValidator());
    }

    public String getSignatureValidationReportHTML() {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        try {
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(signatureValidationReport.getXmlSimpleReport())));
            var xmlSource = new DOMSource(document);

            var xsltFile = new File("src/main/resources/simple-report-bootstrap4.xslt");
            var xsltSource = new StreamSource(xsltFile);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
            transformer.transform(xmlSource, outputTarget);

            var r = outputTarget.getWriter().toString().trim();

            var templateFile = new File("src/main/resources/simple-report-template.html");
            var templateString = new String(java.nio.file.Files.readAllBytes(templateFile.toPath()));
            return templateString.replace("{{content}}", r);

        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            return "Error transforming validation report";
        }
    }

    public Reports getSignatureValidationReport() {
        return signatureValidationReport;
    }

    public boolean hasSignatures() {
        return signatureCheckReport != null && signatureCheckReport.getSimpleReport().getSignaturesCount() > 0;
    }
}
