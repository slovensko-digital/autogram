package digital.slovensko.autogram.core.dto;

import java.util.List;
import java.util.Objects;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.model.DSSDocument;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.SigningParametersResolver;

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
        return new SigningInput(documents, SigningParametersResolver.resolveStrict(parameters, documents));
    }

    public static SigningInput fromDocument(AutogramDocument document, SigningParameters parameters) {
        var documents = List.of(Objects.requireNonNull(document, "document"));
        return new SigningInput(documents, SigningParametersResolver.resolveStrict(parameters, documents));
    }

    public static SigningInput fromFile(AutogramDocument document, SigningParameters parameters) {
        var documents = List.of(document);
        return new SigningInput(documents, SigningParametersResolver.resolveLenientFromFile(parameters, document));
    }

    public static SigningInput fromPDFFile(DSSDocument document, SigningParameters parameters) {
        return fromDocumentLenient(AutogramDocument.build(document, null), parameters);
    }

    public static SigningInput prepareForASiCWithXAdES(AutogramDocument document, SigningParameters parameters) {
        var forced = SigningParametersResolver.withFormAndContainer(parameters, SignatureForm.XAdES, ASiCContainerType.ASiC_E);
        return fromDocumentLenient(document, forced);
    }

    public static SigningInput prepareForASiCWithCAdES(AutogramDocument document, SigningParameters parameters) {
        var forced = SigningParametersResolver.withFormAndContainer(parameters, SignatureForm.CAdES, ASiCContainerType.ASiC_E);
        return fromDocumentLenient(document, forced);
    }

    private static SigningInput fromDocumentLenient(AutogramDocument document, SigningParameters parameters) {
        var documents = List.of(document);
        return new SigningInput(documents, SigningParametersResolver.resolveLenient(parameters, documents));
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
