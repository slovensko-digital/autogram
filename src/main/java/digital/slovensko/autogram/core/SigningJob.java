package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;

public class SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;

    public SigningJob(CommonDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
    }

    public DSSDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    public void onDocumentSignFailed(SigningJob job, SigningError e) {
        responder.onDocumentSignFailed(job, e);
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        responder.onDocumentSigned(signedDocument);
    }
}
