package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ResponderInBatch extends Responder {
    private final Responder responder;
    private final Batch batch;
    private final Integer batchPosition;
    private final Runnable skipAction;
    private final Runnable skipRemainingAction;

    public ResponderInBatch(Responder responder, Batch batch) {
        this(responder, batch, null, null, null);
    }

    public ResponderInBatch(Responder responder, Batch batch, Integer batchPosition, Runnable skipAction,
            Runnable skipRemainingAction) {
        this.responder = responder;
        this.batch = batch;
        this.batchPosition = batchPosition;
        this.skipAction = skipAction;
        this.skipRemainingAction = skipRemainingAction;
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

    @Override
    public boolean isBatch() {
        return true;
    }

    @Override
    public Integer getBatchPosition() {
        return batchPosition;
    }

    @Override
    public Runnable getSkipAction() {
        return skipAction;
    }

    @Override
    public Runnable getSkipRemainingAction() {
        return skipRemainingAction;
    }
}
