package digital.slovensko.autogram.core;

import java.util.List;
import java.util.Objects;

public class AutogramSigningRequest {
    private final List<AutogramDocument> documents;
    private final SigningParameters parameters;

    private AutogramSigningRequest(List<AutogramDocument> documents, SigningParameters parameters) {
        this.documents = List.copyOf(documents);
        this.parameters = Objects.requireNonNull(parameters, "parameters");

        if (this.documents.isEmpty())
            throw new IllegalArgumentException("documents cannot be empty");
    }

    public static AutogramSigningRequest of(List<AutogramDocument> documents, SigningParameters parameters) {
        return new AutogramSigningRequest(documents, parameters);
    }

    public static AutogramSigningRequest forSingleDocument(AutogramDocument document, SigningParameters parameters) {
        return new AutogramSigningRequest(List.of(Objects.requireNonNull(document, "document")), parameters);
    }

    public List<AutogramDocument> getDocuments() {
        return documents;
    }

    public AutogramDocument getFirstDocument() {
        return documents.get(0);
    }

    public AutogramDocument getSingleDocument() {
        if (documents.size() != 1)
            throw new IllegalStateException("This signing request contains multiple documents");

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