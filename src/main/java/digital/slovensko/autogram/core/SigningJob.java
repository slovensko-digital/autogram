package digital.slovensko.autogram.core;

import java.io.File;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;

import static digital.slovensko.autogram.core.AutogramMimeType.isPDF;

public class SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;
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

    public static SigningJob buildFromFile(File file, Responder responder, boolean checkPDFACompliance, SignatureLevel signatureType, boolean isEn319132) {
        var document = new FileDocument(file);
        SigningParameters parameters = getParametersForFile(document, checkPDFACompliance, signatureType, isEn319132);
        return new SigningJob(document, parameters, responder);
    }

    public static SigningJob buildFromFileBatch(File file, Autogram autogram, Responder responder, boolean checkPDFACompliance, SignatureLevel signatureType, boolean isEn319132) {
        var document = new FileDocument(file);
        var parameters = getParametersForFile(document, checkPDFACompliance, signatureType, isEn319132);
        return new SigningJob(document, parameters, responder);
    }

    private static SigningParameters getParametersForFile(FileDocument document, boolean checkPDFACompliance, SignatureLevel signatureType, boolean isEn319132) {
        var level = SignatureValidator.getSignedDocumentSignatureLevel(document);
        if (level != null) switch (level) {
            case PAdES_BASELINE_B:
                return SigningParameters.buildForPDF(document.getName(), checkPDFACompliance, isEn319132);
            case XAdES_BASELINE_B:
                return SigningParameters.buildForASiCWithXAdES(document.getName(), isEn319132);
            case CAdES_BASELINE_B:
                return SigningParameters.buildForASiCWithCAdES(document.getName(), isEn319132);
            default:
                ;
        }

        var filename = document.getName();
        if (isPDF(document.getMimeType())) switch (signatureType) {
            case PAdES_BASELINE_B:
                return SigningParameters.buildForPDF(filename, checkPDFACompliance, isEn319132);
            case XAdES_BASELINE_B:
                return SigningParameters.buildForASiCWithXAdES(filename, isEn319132);
            case CAdES_BASELINE_B:
                return SigningParameters.buildForASiCWithCAdES(filename, isEn319132);
            default:
                ;
        }

        return SigningParameters.buildForASiCWithXAdES(filename, isEn319132);
    }

    public boolean shouldCheckPDFCompliance() {
        return parameters.getCheckPDFACompliance() && isPDF(document.getMimeType());
    }
}
