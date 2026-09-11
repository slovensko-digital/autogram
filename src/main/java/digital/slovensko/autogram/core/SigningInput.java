package digital.slovensko.autogram.core;

import java.util.List;
import java.util.Objects;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.model.DSSDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public class SigningInput {
    private final List<AutogramDocument> documents;
    private final SigningParameters parameters;

    private SigningInput(List<AutogramDocument> documents, SigningParameters parameters) {
        this.documents = List.copyOf(documents);
        this.parameters = Objects.requireNonNull(parameters, "parameters");

        if (this.documents.isEmpty())
            throw new IllegalArgumentException("documents cannot be empty");

        if (this.documents.size() > 1 || this.documents.stream().anyMatch(doc -> doc.isEForm()))
            parameters.setContainer(ASiCContainerType.ASiC_E);
    }

    public static SigningInput of(List<AutogramDocument> documents, SigningParameters parameters) {
        return new SigningInput(documents, parameters);
    }

    public static SigningInput fromDocument(AutogramDocument document, SigningParameters parameters) {
        return new SigningInput(List.of(Objects.requireNonNull(document, "document")), parameters);
    }

    public static SigningInput fromFile(AutogramDocument document, SigningParameters parameters, boolean plainXmlEnabled) {
        var dssDocument = document.toDssDocument();
        var level = SignatureValidator.getSignedDocumentSignatureLevel(
            SignatureValidator.getSignedDocumentSimpleReport(dssDocument));
        if (level != null) switch (level.getSignatureForm()) {
            case PAdES:
                return fromPDFFile(dssDocument, parameters);
            case XAdES:
                return prepareForASiCWithXAdES(document, parameters, plainXmlEnabled);
            case CAdES:
                return prepareForASiCWithCAdES(document, parameters, plainXmlEnabled);
            default:
                ;
        }

        if (isPDF(document.getMimeType())) switch (parameters.getLevel()) {
            case PAdES_BASELINE_B:
                return fromPDFFile(dssDocument, parameters);
            case XAdES_BASELINE_B:
                return prepareForASiCWithXAdES(document, parameters, plainXmlEnabled);
            case CAdES_BASELINE_B:
                return prepareForASiCWithCAdES(document, parameters, plainXmlEnabled);
            default:
                ;
        }

        return prepareForASiCWithXAdES(document, parameters, plainXmlEnabled);
    }

    public static SigningInput fromPDFFile(DSSDocument document, SigningParameters parameters) {
        return fromDocument(AutogramDocument.build(document, null), parameters);
    }

    public static SigningInput prepareForASiCWithXAdES(AutogramDocument document, SigningParameters parameters, boolean plainXmlEnabled) {
        parameters.setContainer(ASiCContainerType.ASiC_E);
        parameters.setSignatureForm(SignatureForm.XAdES);
        return prepareInput(parameters, document, plainXmlEnabled);
    }

    public static SigningInput prepareForASiCWithCAdES(AutogramDocument document, SigningParameters parameters, boolean plainXmlEnabled) {
        parameters.setContainer(ASiCContainerType.ASiC_E);
        parameters.setSignatureForm(SignatureForm.CAdES);
        return prepareInput(parameters, document, plainXmlEnabled);
    }

    private static SigningInput prepareInput(SigningParameters parameters, AutogramDocument document, boolean plainXmlEnabled) {
        return fromDocument(document, parameters);
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