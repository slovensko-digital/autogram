package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.util.Logging;

public abstract class BatchStartCallback {

    protected final Batch batch;
    protected final BatchResponder responder;

    public BatchStartCallback(Batch batch, BatchResponder responder) {
        this.batch = batch;
        this.responder = responder;
    }

    public abstract void accept(SigningKey key);

    public BatchResponder getResponder() {
        return responder;
    }

    public void cancel() {
        try {
            Logging.log("Cancelling batch");
            batch.end();
            responder.onBatchStartFailure(new BatchCanceledException());
        }catch (ResponseNetworkErrorException ex){
            Logging.log("ResponseNetworkErrorException: " + ex.getMessage());
        } catch (Exception e) {
            handleException(e);
        }
    }

    protected void handleException(Exception e) {
        if (e instanceof AutogramException)
            responder.onBatchStartFailure((AutogramException) e);
        else {
            Logging.log("Batch start failed with exception: " + e);
            responder.onBatchStartFailure(new AutogramException("BATCH_START_FAILED", e, e));
        }
    }
}
