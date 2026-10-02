package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.token.PasswordInputCallback;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

public class PasswordManager implements PasswordInputCallback {
    private final UI ui;
    private char[] cachedPassword;
    private Batch cachedBatch;
    private final ThreadLocal<Batch> currentBatch = new ThreadLocal<>();
    private AutogramException contextSpecificPasswordError;

    public PasswordManager(UI ui) {
        this.ui = ui;
    }

    public synchronized char[] getContextSpecificPassword() {
        var previousError = contextSpecificPasswordError;
        contextSpecificPasswordError = null;
        var batch = currentBatch.get();
        if (batch != null) {
            if (cachedBatch == null || !cachedBatch.hasBatchId(batch.getId())) {
                clearCachedPassword();
                cachedBatch = batch;
            }

            if (cachedPassword == null) {
                cachedPassword = ui.getContextSpecificPassword(previousError);
            }
            return cachedPassword;
        }

        return ui.getContextSpecificPassword(previousError);
    }

    public boolean isCachingPIN() {
        return currentBatch.get() != null;
    }

    public <T> T withoutCachedPIN(Callable<T> operation) throws Exception {
        return withBatchContext(null, operation);
    }

    public <T> T withCachedPIN(Batch batch, Callable<T> operation) throws Exception {
        return withBatchContext(Objects.requireNonNull(batch), operation);
    }

    private <T> T withBatchContext(Batch batch, Callable<T> operation) throws Exception {
        var previousBatch = currentBatch.get();
        currentBatch.set(batch);
        try {
            return operation.call();
        } finally {
            if (previousBatch == null)
                currentBatch.remove();
            else
                currentBatch.set(previousBatch);
        }
    }

    public synchronized void onContextSpecificPasswordRejected(AutogramException error) {
        clearCachedPassword();
        cachedBatch = null;
        contextSpecificPasswordError = Objects.requireNonNull(error);
    }

    public synchronized void clearContextSpecificPasswordError() {
        contextSpecificPasswordError = null;
    }

    public synchronized void reset() {
        clearCachedPassword();
        cachedBatch = null;
        contextSpecificPasswordError = null;
    }

    private void clearCachedPassword() {
        if (cachedPassword != null)
            Arrays.fill(cachedPassword, '\0');

        cachedPassword = null;
    }

    @Override
    public char[] getPassword() {
        return ui.getKeystorePassword();
    }
}
