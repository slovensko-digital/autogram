package digital.slovensko.autogram.core;

import java.io.File;
import java.util.List;
import java.util.Objects;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.xdc.XDCBuilder;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import static digital.slovensko.autogram.core.AutogramMimeType.*;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.EMPTY_DOCUMENT;
import static digital.slovensko.autogram.util.DSSUtils.getXdcfFilename;

public class SigningInput {
    private final List<AutogramDocument> documents;
    private final SigningParameters parameters;

    private SigningInput(List<AutogramDocument> documents, SigningParameters parameters) {
        this.documents = List.copyOf(documents);
        this.parameters = Objects.requireNonNull(parameters, "parameters");

        if (this.documents.isEmpty())
            throw new IllegalArgumentException("documents cannot be empty");
    }

    public static SigningInput of(List<AutogramDocument> documents, SigningParameters parameters) {
        return new SigningInput(documents, parameters);
    }

    public static SigningInput fromDocument(AutogramDocument document, SigningParameters parameters) {
        return new SigningInput(List.of(prepareDocument(Objects.requireNonNull(document, "document"), parameters)),
                parameters);
    }

    public static SigningInput fromFile(File file, boolean checkPDFACompliance, SignatureLevel signatureType,
            boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        var document = AutogramDocument.fromFile(file);
        return prepareForFile(document, checkPDFACompliance, signatureType, isEn319132, tspSource, plainXmlEnabled);
    }

    public static SigningInput prepareForPDF(AutogramDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource) {
        return prepareInput(tspSource == null ? SignatureLevel.PAdES_BASELINE_B : SignatureLevel.PAdES_BASELINE_T,
            DigestAlgorithm.SHA256, null, null, signAsEn319132, null, null, null, document.getEFormAttributes(), false,
            null,
            checkPDFACompliance, 640, document, tspSource, true);
    }

    public static SigningInput prepareForPDF(DSSDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource) {
        return prepareForPDF(AutogramDocument.fromDssDocument(document), checkPDFACompliance, signAsEn319132,
            tspSource);
    }

    public static SigningInput prepareForASiCWithXAdES(AutogramDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        return prepareInput(tspSource == null ? SignatureLevel.XAdES_BASELINE_B : SignatureLevel.XAdES_BASELINE_T,
            DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, signAsEn319132,
            null, null, null, document.getEFormAttributes(), true,
            EFormUtils.getFsFormIdFromFilename(document.getName()),
            checkPDFACompliance, 640, document, tspSource, plainXmlEnabled);
    }

    public static SigningInput prepareForASiCWithXAdES(DSSDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        return prepareForASiCWithXAdES(AutogramDocument.fromDssDocument(document), checkPDFACompliance,
            signAsEn319132, tspSource, plainXmlEnabled);
    }

    public static SigningInput prepareForASiCWithCAdES(AutogramDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        return prepareInput(tspSource == null ? SignatureLevel.CAdES_BASELINE_B : SignatureLevel.CAdES_BASELINE_T,
            DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, signAsEn319132,
            null, null, null, document.getEFormAttributes(), true,
            EFormUtils.getFsFormIdFromFilename(document.getName()),
            checkPDFACompliance, 640, document, tspSource, plainXmlEnabled);
    }

        public static SigningInput prepareForASiCWithCAdES(DSSDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        return prepareForASiCWithCAdES(AutogramDocument.fromDssDocument(document), checkPDFACompliance,
            signAsEn319132, tspSource, plainXmlEnabled);
        }

        public static SigningInput prepare(SignatureLevel level, DigestAlgorithm digestAlgorithm,
            ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization,
            String keyInfoCanonicalization, EFormAttributes eFormAttributes, boolean autoLoadEform, String fsFormId,
            boolean checkPDFACompliance, int visualizationWidth, DSSDocument document, TSPSource tspSource,
            boolean plainXmlEnabled) {
        if (document == null)
            throw new SigningParametersException(EMPTY_DOCUMENT);

        return prepareInput(level, digestAlgorithm, container, packaging, en319132, infoCanonicalization,
            propertiesCanonicalization, keyInfoCanonicalization, eFormAttributes, autoLoadEform, fsFormId,
            checkPDFACompliance, visualizationWidth, AutogramDocument.fromDssDocument(document), tspSource,
            plainXmlEnabled);
    }

    private static SigningInput prepareInput(SignatureLevel level, DigestAlgorithm digestAlgorithm,
            ASiCContainerType container, SignaturePackaging packaging, boolean en319132,
            String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            EFormAttributes eFormAttributes, boolean autoLoadEform, String fsFormId, boolean checkPDFACompliance,
            int visualizationWidth, AutogramDocument document, TSPSource tspSource, boolean plainXmlEnabled) {
        var resolvedDigestAlgorithm = digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
        var preparedDocument = document.prepareEFormAttributes(eFormAttributes, autoLoadEform, fsFormId,
            propertiesCanonicalization, resolvedDigestAlgorithm, plainXmlEnabled);
        var parameters = SigningParameters.buildParameters(level, resolvedDigestAlgorithm, container, packaging,
            en319132, infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization,
            shouldCreateXdc(preparedDocument.getEFormAttributes()), checkPDFACompliance, visualizationWidth, tspSource);
        return fromDocument(preparedDocument, parameters);
    }

    public static AutogramDocument prepareDocument(AutogramDocument document, SigningParameters parameters) {
        var dssDocument = document.toDssDocument();
        var eFormAttributes = document.getEFormAttributes();

        if (shouldCreateXdc(eFormAttributes) && !isXDC(dssDocument.getMimeType()) && !isAsice(dssDocument.getMimeType()))
            dssDocument = XDCBuilder.transform(eFormAttributes, parameters.getPropertiesCanonicalization(),
                    parameters.getDigestAlgorithm(), dssDocument.getName(), EFormUtils.getXmlFromDocument(dssDocument));

        if (isXDC(dssDocument.getMimeType())) {
            dssDocument.setMimeType(AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET);
            dssDocument.setName(getXdcfFilename(dssDocument.getName()));
        }

        return AutogramDocument.fromDssDocument(dssDocument).withEFormAttributes(document.getEFormAttributes());
    }

    private static boolean shouldCreateXdc(EFormAttributes eFormAttributes) {
        return eFormAttributes != null && eFormAttributes.containerXmlns() != null
                && eFormAttributes.containerXmlns().contains("xmldatacontainer");
    }

    private static SigningInput prepareForFile(AutogramDocument document, boolean checkPDFACompliance,
            SignatureLevel signatureType, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        var dssDocument = document.toDssDocument();
        var level = SignatureValidator.getSignedDocumentSignatureLevel(
            SignatureValidator.getSignedDocumentSimpleReport(dssDocument));
        if (level != null) switch (level.getSignatureForm()) {
            case PAdES:
                return prepareForPDF(document, checkPDFACompliance, isEn319132, tspSource);
            case XAdES:
                return prepareForASiCWithXAdES(document, checkPDFACompliance, isEn319132, tspSource, plainXmlEnabled);
            case CAdES:
                return prepareForASiCWithCAdES(document, checkPDFACompliance, isEn319132, tspSource, plainXmlEnabled);
            default:
                ;
        }

        if (isPDF(document.getMimeType())) switch (signatureType) {
            case PAdES_BASELINE_B:
                return prepareForPDF(document, checkPDFACompliance, isEn319132, tspSource);
            case XAdES_BASELINE_B:
                return prepareForASiCWithXAdES(document, checkPDFACompliance, isEn319132, tspSource, plainXmlEnabled);
            case CAdES_BASELINE_B:
                return prepareForASiCWithCAdES(document, checkPDFACompliance, isEn319132, tspSource, plainXmlEnabled);
            default:
                ;
        }

        return prepareForASiCWithXAdES(document, checkPDFACompliance, isEn319132, tspSource, plainXmlEnabled);
    }

    public List<AutogramDocument> getDocuments() {
        return documents;
    }

    public AutogramDocument getFirstDocument() {
        return documents.get(0);
    }

    public AutogramDocument getSingleDocument() {
        if (documents.size() != 1)
            throw new IllegalStateException("This signing input contains multiple documents");

        return documents.get(0);
    }

    public int getDocumentCount() {
        return documents.size();
    }

    public boolean isMultiDocument() {
        return documents.size() > 1;
    }

    public SigningParameters getParameters() {
        return parameters;
    }
}