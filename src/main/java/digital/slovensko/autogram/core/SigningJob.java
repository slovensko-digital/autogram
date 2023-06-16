package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.SaveFileResponder;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;

    public SigningJob(CommonDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
    }

    public DSSDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    public boolean isPlainText() {
        if (parameters.getTransformationOutputMimeType() != null)
            return parameters.getTransformationOutputMimeType().equals(MimeTypeEnum.TEXT);

        return document.getMimeType().equals(MimeTypeEnum.TEXT);
    }

    public boolean isHTML() {
        if (parameters.getTransformationOutputMimeType() != null)
            return parameters.getTransformationOutputMimeType().equals(MimeTypeEnum.HTML);

        return false;
    }

    public int getVisualizationWidth() {
        return parameters.getVisualizationWidth();
    }

    public boolean isPDF() {
        return document.getMimeType().equals(MimeTypeEnum.PDF);
    }

    public boolean isImage() {
        return document.getMimeType().equals(MimeTypeEnum.JPEG) || document.getMimeType().equals(MimeTypeEnum.PNG);
    }

    private boolean isXDC() {
        return document.getMimeType()
            .equals(AutogramMimeType.XML_DATACONTAINER);
    }

    public String getDocumentAsPlainText() {
        if (document.getMimeType().equals(MimeTypeEnum.TEXT)) {
            try {
                return new String(document.openStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return transform();
        }
    }

    private String transform() {
        // TODO probably move this logic into signing job creation
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(this.document.openStream()));

            var xmlSource = new DOMSource(document);
            if (isXDC())
                xmlSource = XDCTransformer.extractFromXDC(document, builderFactory);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = TransformerFactory.newInstance().newTransformer(
                new StreamSource(new ByteArrayInputStream(parameters.getTransformation().getBytes())));
            transformer.transform(xmlSource, outputTarget);

            return outputTarget.getWriter().toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // TODO
        }
    }

    public String getDocumentAsHTML() {
        return transform();
    }

    public String getDocumentAsBase64Encoded() {
        try {
            return new String(Base64.getEncoder().encode(document.openStream().readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void signWithKeyAndRespond(SigningKey key) {
        boolean isContainer = getParameters().getContainer() != null;
        var doc = switch (getParameters().getSignatureType()) {
            case XAdES -> isContainer ? signDocumentAsAsiCWithXAdeS(key) : signDocumentAsXAdeS(key);
            case CAdES -> isContainer ? signDocumentAsASiCWithCAdeS(key) : signDocumentAsCAdeS(key);
            case PAdES -> signDocumentAsPAdeS(key);
            default -> throw new RuntimeException("Unsupported signature type: " + getParameters().getSignatureType());
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
        if (getParameters().shouldCreateDatacontainer() && !isXDC()) {
            var transformer = XDCTransformer.buildFromSigningParameters(getParameters());
            doc = transformer.transform(getDocument());
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

    public static SigningJob buildFromFile(File file, Autogram autogram) {
        var document = new FileDocument(file);

        SigningParameters parameters;
        var filename = file.getName();

        if (filename.endsWith(".pdf")) {
            parameters = SigningParameters.buildForPDF(filename);
        } else {
            parameters = SigningParameters.buildForASiCWithXAdES(filename);
        }

        var responder = new SaveFileResponder(file, autogram);
        return new SigningJob(document, parameters, responder);
    }

    public boolean shouldCheckPDFCompliance() {
        return parameters.getCheckPDFACompliance();
    }
}
