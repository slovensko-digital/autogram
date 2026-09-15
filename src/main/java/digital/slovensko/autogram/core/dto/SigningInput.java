package digital.slovensko.autogram.core.dto;

import java.util.List;
import java.util.Objects;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.UnknownEformException;

import static digital.slovensko.autogram.core.dto.AutogramMimeType.*;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.INVALID_PACKAGING;

public class SigningInput {
    private final List<AutogramDocument> documents;
    private final SigningParameters parameters;

    private SigningInput(List<AutogramDocument> documents, SigningParameters parameters) {
        this.documents = List.copyOf(documents);
        this.parameters = Objects.requireNonNull(parameters, "parameters");

        if (this.documents.isEmpty())
            throw new IllegalArgumentException("documents cannot be empty");

        if (!parameters.isPlainXmlEnabled() && this.documents.stream()
                .anyMatch(document -> isXML(document.getMimeType()) && !document.isEForm()))
            throw new UnknownEformException();

        if (this.documents.size() > 1
            || this.documents.stream().anyMatch(document -> document.isEForm() || isAsice(document.getMimeType())))
            parameters.setContainer(ASiCContainerType.ASiC_E);

        if (parameters.getSignatureForm() == SignatureForm.XAdES
            && parameters.getContainer() == null
            && parameters.getSignaturePackaging() != SignaturePackaging.ENVELOPING
            && this.documents.stream().anyMatch(document -> !isXML(document.getMimeType())
                && !isXDC(document.getMimeType()) && !isAsice(document.getMimeType())))
            throw new SigningParametersException(INVALID_PACKAGING);
    }

    public static SigningInput of(List<AutogramDocument> documents, SigningParameters parameters) {
        return new SigningInput(documents, parameters);
    }

    public static SigningInput fromDocument(AutogramDocument document, SigningParameters parameters) {
        return new SigningInput(List.of(Objects.requireNonNull(document, "document")), parameters);
    }

    public static SigningInput fromFile(AutogramDocument document, SigningParameters parameters) {
        var dssDocument = document.toDssDocument();
        var signedDocumentSignature = SignatureValidator.getSignedDocumentSignature(dssDocument);
        if (signedDocumentSignature != null) {
            parameters.setSignatureForm(signedDocumentSignature.form());
            parameters.setContainer(signedDocumentSignature.container());
            if (signedDocumentSignature.packaging() != null)
                parameters.setSignaturePackaging(signedDocumentSignature.packaging());

            if (signedDocumentSignature.container() == null)
                return signedDocumentSignature.form() == SignatureForm.PAdES
                        ? fromPDFFile(dssDocument, parameters)
                        : fromDocument(document, parameters);

            return switch (signedDocumentSignature.form()) {
                case XAdES -> prepareForASiCWithXAdES(document, parameters);
                case CAdES -> prepareForASiCWithCAdES(document, parameters);
                default -> fromDocument(document, parameters);
            };
        }

        if (isPDF(document.getMimeType())) switch (parameters.getLevel()) {
            case PAdES_BASELINE_B:
                return fromPDFFile(dssDocument, parameters);
            case XAdES_BASELINE_B:
                return prepareForASiCWithXAdES(document, parameters);
            case CAdES_BASELINE_B:
                return prepareForASiCWithCAdES(document, parameters);
            default:
                ;
        }

        return prepareForASiCWithXAdES(document, parameters);
    }

    public static SigningInput fromPDFFile(DSSDocument document, SigningParameters parameters) {
        return fromDocument(AutogramDocument.build(document, null), parameters);
    }

    public static SigningInput prepareForASiCWithXAdES(AutogramDocument document, SigningParameters parameters) {
        parameters.setContainer(ASiCContainerType.ASiC_E);
        parameters.setSignatureForm(SignatureForm.XAdES);
        return fromDocument(document, parameters);
    }

    public static SigningInput prepareForASiCWithCAdES(AutogramDocument document, SigningParameters parameters) {
        parameters.setContainer(ASiCContainerType.ASiC_E);
        parameters.setSignatureForm(SignatureForm.CAdES);
        return fromDocument(document, parameters);
    }

    public int getPreviewDocumentsCount() {
        if (documents.size() == 1 && documents.get(0).isAsice())
            return documents.get(0).getOriginalDocuments().size();

        return documents.size();
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

    public String getName() {
        return documents.get(0).getName();
    }

    public SigningParameters getParameters() {
        return parameters;
    }
}