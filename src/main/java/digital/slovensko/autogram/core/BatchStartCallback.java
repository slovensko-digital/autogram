package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.util.Logging;

public class BatchStartCallback {

    private final Batch batch;
    private final SigningResponder responder;

    public BatchStartCallback(Batch batch, SigningResponder responder) {
        this.batch = batch;
        this.responder = responder;
    }

    public void accept(SigningKey key) {
        try {
            Logging.log("Starting batch");
            batch.start(key);
            responder.onBatchStarted(batch, SigningMode.AUTOMATED);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void cancel() {
        try {
            Logging.log("Cancelling batch");
            batch.end();
            responder.onBatchStartFailed(new BatchCanceledException());
        }catch (ResponseNetworkErrorException ex){
            Logging.log("ResponseNetworkErrorException: " + ex.getMessage());
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        batch.end();
        if (e instanceof AutogramException)
            responder.onBatchStartFailed((AutogramException) e);
        else {
            Logging.log("Batch start failed with exception: " + e);
            responder.onBatchStartFailed(new AutogramException("BATCH_START_FAILED", e, e));
        }
    }
}
