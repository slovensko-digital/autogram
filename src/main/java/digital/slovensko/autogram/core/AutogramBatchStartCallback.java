package digital.slovensko.autogram.core;

import java.util.function.Consumer;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.gui.GUI;
import digital.slovensko.autogram.util.Logging;

public class AutogramBatchStartCallback implements Consumer<SigningKey> {

    private final Batch batch;
    private final BatchResponder responder;

    public AutogramBatchStartCallback(Batch batch, BatchResponder responder) {
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
