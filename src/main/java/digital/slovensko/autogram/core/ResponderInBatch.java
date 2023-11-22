package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ResponderInBatch extends Responder {
    private final Responder responder;
    private final Batch batch;

    public ResponderInBatch(Responder responder, Batch batch) {
        this.responder = responder;
        this.batch = batch;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        batch.onJobSuccess();
        responder.onDocumentSigned(signedDocument);
    }

    public void onDocumentSignFailed(AutogramException error) {
        batch.onJobFailure();
        if (!error.batchCanContinue())
            batch.end();

        responder.onDocumentSignFailed(error);
    }
}
