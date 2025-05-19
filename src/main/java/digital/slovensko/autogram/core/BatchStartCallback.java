package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.ui.gui.GUI;
import digital.slovensko.autogram.util.Logging;

public class BatchStartCallback {

    private final Batch batch;
    private final BatchResponder responder;

    public BatchStartCallback(Batch batch, BatchResponder responder) {
        this.batch = batch;
        this.responder = responder;
    }

    public void accept(SigningKey key) {
        GUI.assertOnWorkThread();
        try {
            Logging.log("Starting batch");
            batch.start(key);
            handleSuccess();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void cancel() {
        GUI.assertOnWorkThread();
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

    private void handleException(Exception e) {
        if (e instanceof AutogramException)
            responder.onBatchStartFailure((AutogramException) e);
        else {
            Logging.log("Batch start failed with exception: " + e);
            responder.onBatchStartFailure(
                    new AutogramException("Unkown error occured while starting batch", "",
                            "Batch start failed with exception: " + e, e));
        }
    }

    private void handleSuccess() {
        responder.onBatchStartSuccess(batch);
    }
};
