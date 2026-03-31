package digital.slovensko.autogram.core;

import digital.slovensko.autogram.util.Logging;

public class BatchModeStartCallback extends BatchStartCallback {

    public BatchModeStartCallback(Batch batch, BatchResponder responder) {
        super(batch, responder);
    }

    @Override
    public void accept(SigningKey key) {
        try {
            Logging.log("Starting batch (all at once)");
            batch.start(key);
            responder.onBatchStartSuccess(batch);
        } catch (Exception e) {
            handleException(e);
        }
    }
}
