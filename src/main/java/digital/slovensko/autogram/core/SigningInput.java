package digital.slovensko.autogram.core;

import java.io.File;
import java.util.List;
import java.util.Objects;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.xdc.XDCBuilder;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import static digital.slovensko.autogram.core.AutogramMimeType.*;
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
        var preparedParameters = getParametersForFile(document.toDssDocument(), checkPDFACompliance, signatureType,
                isEn319132, tspSource, plainXmlEnabled);
        return fromDocument(document.withEFormAttributes(preparedParameters.eFormAttributes()),
            preparedParameters.signingParameters());
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

        private static SigningParameters.PreparedParameters getParametersForFile(DSSDocument document,
            boolean checkPDFACompliance,
            SignatureLevel signatureType, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        var level = SignatureValidator.getSignedDocumentSignatureLevel(SignatureValidator.getSignedDocumentSimpleReport(document));
        if (level != null) switch (level.getSignatureForm()) {
            case PAdES:
                return SigningParameters.prepareForPDF(document, checkPDFACompliance, isEn319132, tspSource);
            case XAdES:
                return SigningParameters.prepareForASiCWithXAdES(document, checkPDFACompliance, isEn319132,
                    tspSource, plainXmlEnabled);
            case CAdES:
                return SigningParameters.prepareForASiCWithCAdES(document, checkPDFACompliance, isEn319132,
                    tspSource, plainXmlEnabled);
            default:
                ;
        }

        if (isPDF(document.getMimeType())) switch (signatureType) {
            case PAdES_BASELINE_B:
                return SigningParameters.prepareForPDF(document, checkPDFACompliance, isEn319132, tspSource);
            case XAdES_BASELINE_B:
                return SigningParameters.prepareForASiCWithXAdES(document, checkPDFACompliance, isEn319132,
                    tspSource, plainXmlEnabled);
            case CAdES_BASELINE_B:
                return SigningParameters.prepareForASiCWithCAdES(document, checkPDFACompliance, isEn319132,
                    tspSource, plainXmlEnabled);
            default:
                ;
        }

        return SigningParameters.prepareForASiCWithXAdES(document, checkPDFACompliance, isEn319132, tspSource,
            plainXmlEnabled);
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