package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ResponderBatch extends Responder {
    private final Responder responder;
    private final Batch batch;

    public ResponderBatch(Responder responder, Batch batch) {
        this.responder = responder;
        this.batch = batch;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        batch.onJobSuccess();
        responder.onDocumentSigned(signedDocument);
    }

    public void onDocumentSignFailed(AutogramException error) {
        batch.onJobFailure();
        responder.onDocumentSignFailed(error);
    }
}
