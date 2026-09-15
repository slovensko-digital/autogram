package digital.slovensko.autogram.core;

import java.util.List;

import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
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
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class SigningJob {
    private final Responder responder;
    private final SigningInput input;

    private SigningJob(SigningInput input, Responder responder) {
        this.input = input;
        this.responder = responder;
    }

    public AutogramDocument getDocument() {
        return input.getFirstDocument();
    }

    public List<AutogramDocument> getDocuments() {
        return input.getDocuments();
    }

    private List<DSSDocument> getDssDocuments() {
        return getDocuments().stream().map(AutogramDocument::toDssDocument).toList();
    }

    private DSSDocument getDssDocument() {
        return getDocument().toDssDocument();
    }

    public SigningParameters getParameters() {
        return input.getParameters();
    }

    public int getVisualizationWidth() {
        return getParameters().getVisualizationWidth();
    }

    public boolean isMultiDocument() {
        return input.isMultiDocument();
    }

    public int getPreviewDocumentsCount() {
        return input.getPreviewDocumentsCount();
    }

    public String getName() {
        return input.getName();
    }

    public void signWithKeyAndRespond(SigningKey key, TSPSource tspSource) throws InterruptedException, AutogramException {
        Logging.log("Signing Job: " + this.hashCode() + " file " + getName()
            + (input.isMultiDocument() ? " documents=" + input.getDocumentCount() : ""));

        boolean isContainer = getParameters().getContainer() != null;
        var doc = switch (getParameters().getSignatureForm()) {
            case XAdES -> isContainer ? signDocumentAsAsiCWithXAdeS(key, tspSource) : signDocumentAsXAdeS(key, tspSource);
            case CAdES -> isContainer ? signDocumentAsASiCWithCAdeS(key, tspSource) : signDocumentAsCAdeS(key, tspSource);
            case PAdES -> signDocumentAsPAdeS(key, tspSource);
            default -> throw new RuntimeException(
                    "Unsupported signature type: " + getParameters().getSignatureForm());
        };
        responder.onDocumentSigned(new SignedDocument(doc, key.getCertificate()));
    }

    public void onDocumentSignFailed(AutogramException e) {
        responder.onDocumentSignFailed(e);
    }

    private DSSDocument signDocumentAsCAdeS(SigningKey key, TSPSource tspSource) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new CAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createCAdESSignatureParameters(input);

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.XAdES_BASELINE_T))
            service.setTspSource(tspSource);

        var document = getDssDocument(); 
        var dataToSign = service.getDataToSign(document, signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(document, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningKey key, TSPSource tspSource) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new ASiCWithXAdESService(commonCertificateVerifier);
        var signatureParameters = DssSigningParametersFactory.createASiCWithXAdESSignatureParameters(input);
        var documents = getDssDocuments();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.XAdES_BASELINE_T))
            service.setTspSource(tspSource);

        var dataToSign = service.getDataToSign(documents, signatureParameters);
        var signatureValue = key.sign(dataToSign, getParameters().getDigestAlgorithm());

        return service.signDocument(documents, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsXAdeS(SigningKey key, TSPSource tspSource) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new XAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createXAdESSignatureParameters(input);

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.XAdES_BASELINE_T))
            service.setTspSource(tspSource);

        var document = getDssDocument();
        var dataToSign = service.getDataToSign(document, signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(document, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsASiCWithCAdeS(SigningKey key, TSPSource tspSource) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new ASiCWithCAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createASiCWithCAdESSignatureParameters(input);
        var documents = getDssDocuments();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.CAdES_BASELINE_T))
            service.setTspSource(tspSource);

        var dataToSign = service.getDataToSign(documents, signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(documents, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsPAdeS(SigningKey key, TSPSource tspSource) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level
        var service = new PAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = DssSigningParametersFactory.createPAdESSignatureParameters(input);

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.PAdES_BASELINE_T)) {
            service.setTspSource(tspSource);
            signatureParameters.setContentSize(9472*2);
        }

        var document = getDssDocument();
        var dataToSign = service.getDataToSign(document, signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(document, signatureParameters, signatureValue);
    }

    public static SigningJob fromInput(SigningInput input, Responder responder) {
        return new SigningJob(input, responder);
    }

    public boolean shouldCheckPDFCompliance() {
        return getParameters().getCheckPDFACompliance() && getDocuments().stream().anyMatch(d -> d.isPDF());
    }
}
