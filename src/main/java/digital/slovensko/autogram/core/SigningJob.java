package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.SaveFileResponder;
import digital.slovensko.autogram.util.DocumentUtils;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.xades.signature.XAdESService;

import java.io.*;
import java.util.List;

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
        if (parameters.getTransformationOutputMimeType() != null && document.getMimeType() != MimeTypeEnum.ASICE)
            return parameters.getTransformationOutputMimeType().equals(MimeTypeEnum.TEXT);

        return DocumentUtils.isPlainText(document);
    }

    public boolean isHTML() {
        if (parameters.getTransformationOutputMimeType() != null && document.getMimeType() != MimeTypeEnum.ASICE)
            return parameters.getTransformationOutputMimeType().equals(MimeTypeEnum.HTML);

        return false;
    }

    public int getVisualizationWidth() {
        return parameters.getVisualizationWidth();
    }

    public boolean isPDF() {
        return DocumentUtils.isPdf(document);
    }

    public boolean isImage() {
        return DocumentUtils.isImage(document);
    }

    private boolean isXDC() {
        return DocumentUtils.isXDC(document);
    }

    public boolean isAsice() {
        return DocumentUtils.isAsice(document);
    }

    public String getDocumentAsPlainText() {
        return DocumentUtils.getDocumentAsPlainText(document, parameters.getTransformation());
    }

    public String getDocumentAsHTML() {
        return DocumentUtils.transform(document, parameters.getTransformation());
    }

    public String getDocumentAsBase64Encoded() {
        return DocumentUtils.getDocumentAsBase64Encoded(document);
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

        var dataToSign = service.getDataToSign(getOriginalDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getOriginalDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningKey key) {
        DSSDocument doc = getOriginalDocument();
        if (getParameters().shouldCreateDatacontainer() && !isXDC()) {
            var transformer = XDCTransformer.buildFromSigningParameters(getParameters());
            doc = transformer.transform(getOriginalDocument());
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

        var dataToSign = service.getDataToSign(getOriginalDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getOriginalDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsASiCWithCAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithCAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getASiCWithCAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getOriginalDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getOriginalDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument getOriginalDocument() {
        if (isAsice()) {
            SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(getDocument());
            documentValidator.setCertificateVerifier(new CommonCertificateVerifier());
            List<AdvancedSignature> signatures = documentValidator.getSignatures();
            AdvancedSignature advancedSignature = signatures.get(0);
            List<DSSDocument> originalDocuments = documentValidator.getOriginalDocuments(advancedSignature.getId());
            return originalDocuments.get(0);
        } else {
            return getDocument();
        }
    }

    private DSSDocument signDocumentAsPAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new PAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getPAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getOriginalDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getOriginalDocument(), signatureParameters, signatureValue);
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
