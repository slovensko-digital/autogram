package digital.slovensko.autogram.core;

import java.util.List;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.alert.LogOnStatusAlert;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public class SigningJob {
    private final Responder responder;
    private final SigningInput input;

    private SigningJob(SigningInput input, Responder responder) {
        this.input = input;
        this.responder = responder;
    }

    public DSSDocument getDocument() {
        return input.getFirstDocument().toDssDocument();
    }

    public List<DSSDocument> getDocuments() {
        return input.getDocuments().stream().map(AutogramDocument::toDssDocument).toList();
    }

    public List<AutogramDocument> getAutogramDocuments() {
        return input.getDocuments();
    }

    public List<DSSDocument> getDocumentsForContentChecks() {
        return getDocuments();
    }

    public SigningParameters getParameters() {
        return input.getParameters();
    }

    public int getVisualizationWidth() {
        return getParameters().getVisualizationWidth();
    }

    public void signWithKeyAndRespond(SigningKey key) throws InterruptedException, AutogramException {

        Logging.log("Signing Job: " + this.hashCode() + " file " + getDocument().getName()
            + (input.isMultiDocument() ? " documents=" + input.getDocumentCount() : ""));
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
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new CAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createCAdESSignatureParameters(input);

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new ASiCWithXAdESService(commonCertificateVerifier);
        var signatureParameters = DssSigningParametersFactory.createASiCWithXAdESSignatureParameters(input);
        var documents = getDocuments();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.XAdES_BASELINE_T))
            service.setTspSource(getParameters().getTspSource());

        var dataToSign = service.getDataToSign(documents, signatureParameters);
        var signatureValue = key.sign(dataToSign, getParameters().getDigestAlgorithm());

        return service.signDocument(documents, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsXAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new XAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createXAdESSignatureParameters(input);

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsASiCWithCAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new ASiCWithCAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createASiCWithCAdESSignatureParameters(input);
        var documents = getDocuments();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.CAdES_BASELINE_T))
            service.setTspSource(getParameters().getTspSource());

        var dataToSign = service.getDataToSign(documents, signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(documents, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsPAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new PAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createPAdESSignatureParameters(input);

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.PAdES_BASELINE_T)) {
            service.setTspSource(getParameters().getTspSource());
            signatureParameters.setContentSize(9472*2);
        }

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    public static SigningJob fromInput(SigningInput input, Responder responder) {
        return new SigningJob(input, responder);
    }

    public boolean shouldCheckPDFCompliance() {
        return getParameters().getCheckPDFACompliance()
                && getDocumentsForContentChecks().stream()
                        .anyMatch(document -> document.getMimeType() != null && isPDF(document.getMimeType()));
    }
}
