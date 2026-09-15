package digital.slovensko.autogram.core;

import java.util.List;

import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.util.AsicContainerUtils;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.alert.LogOnStatusAlert;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.common.signature.AbstractASiCSignatureService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.signature.AbstractSignatureService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class SigningJob {
    private final Responder responder;
    private final SigningInput input;
    private final List<AutogramDocument> documentsToVisualize;
    private List<Visualization> visualizations;

    private SigningJob(SigningInput input, List<AutogramDocument> documentsToVisualize, Responder responder) {
        this.input = input;
        this.documentsToVisualize = documentsToVisualize;
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

    public List<Visualization> getVisualizations() {
        return visualizations;
    }

    public SigningParameters getParameters() {
        return input.getParameters();
    }

    public int getVisualizationWidth() {
        return getParameters().getVisualizationWidth();
    }

    public boolean isMultiDocument() {
        return documentsToVisualize.size() > 1;
    }

    public int getPreviewDocumentsCount() {
        return input.getPreviewDocumentsCount();
    }

    public String getName() {
        return input.getName();
    }

    public void initializeVisualizations() throws OriginalDocumentNotFoundException, FailedVisualizationException {
        visualizations = documentsToVisualize.stream()
            .map(DocumentVisualizationBuilder::fromDocument)
            .toList();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void signWithKeyAndRespond(SigningKey key, TSPSource tspSource) throws InterruptedException, AutogramException {
        Logging.log("Signing Job: " + this.hashCode() + " file " + getName()
            + (isMultiDocument() ? " documents=" + input.getDocumentCount() : ""));

        var signatureParameters = DssSigningParametersFactory.createSignatureParameters(input, key);
        var signatureService = createSignatureService(tspSource);

        if (input.getParameters().getContainer() == ASiCContainerType.ASiC_E) {
            var castedService = (AbstractASiCSignatureService) signatureService;
            var documents = getDssDocuments();

            var dataToSign = castedService.getDataToSign(documents, signatureParameters);
            var signatureValue = key.sign(dataToSign, getParameters().getDigestAlgorithm());
            var signedDocument = castedService.signDocument(documents, signatureParameters, signatureValue);
            responder.onDocumentSigned(new SignedDocument(signedDocument, key.getCertificate()));

        } else {
            var document = getDssDocument();

            var dataToSign = signatureService.getDataToSign(document, signatureParameters);
            var signatureValue = key.sign(dataToSign, getParameters().getDigestAlgorithm());
            var signedDocument = signatureService.signDocument(document, signatureParameters, signatureValue);
            responder.onDocumentSigned(new SignedDocument(signedDocument, key.getCertificate()));
        }
    }

    public void onDocumentSignFailed(AutogramException e) {
        responder.onDocumentSignFailed(e);
    }

    public static SigningJob fromInput(SigningInput input, Responder responder) {
        List<AutogramDocument> documentsToVisualize;
        if (input.getDocuments().size() == 1 && input.getFirstDocument().isAsice()) {
            documentsToVisualize = AsicContainerUtils.getOriginalDocuments(input.getFirstDocument()).stream().toList();
        } else {
            documentsToVisualize = input.getDocuments();
        }

        return new SigningJob(input, documentsToVisualize, responder);
    }

    public boolean shouldCheckPDFCompliance() {
        return getParameters().getCheckPDFACompliance() && getDocuments().stream().anyMatch(d -> d.isPDF());
    }

    @SuppressWarnings("rawtypes")
    private AbstractSignatureService createSignatureService(TSPSource tspSource) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setAlertOnExpiredCertificate(new LogOnStatusAlert()); // expired certificates are filtered on UI level

        var isContainer = input.getParameters().getContainer() != null;
        if (!isContainer && input.getDocuments().size() > 1)
            throw new AutogramException("Multiple documents are not allowed for non-container signatures");

        var service = switch (input.getParameters().getSignatureForm()) {
            case XAdES -> isContainer ? new ASiCWithXAdESService(commonCertificateVerifier) : new XAdESService(commonCertificateVerifier);
            case CAdES -> isContainer ? new ASiCWithCAdESService(commonCertificateVerifier) : new CAdESService(commonCertificateVerifier);
            case PAdES -> new PAdESService(commonCertificateVerifier);
            default -> throw new RuntimeException("Unsupported signature type: " + input.getParameters().getSignatureForm());
        };

        if (input.getParameters().getSignatureProfile().equals(SignatureProfile.BASELINE_T) && tspSource != null)
            service.setTspSource(tspSource);

        return service;
    }
}
