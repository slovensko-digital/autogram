package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.PasswordInputCallback;

import java.util.Arrays;
import java.util.Objects;

public class PasswordManager implements PasswordInputCallback {
    private final UI ui;
    private char[] cachedPassword;
    private Batch cachedBatch;
    private final ThreadLocal<Batch> currentBatch = new ThreadLocal<>();

    /** The settings argument is retained for source compatibility; caching is flow-scoped. */
    public PasswordManager(UI ui) {
        this.ui = ui;
    }

    public synchronized char[] getContextSpecificPassword() {
        var batch = currentBatch.get();
        if (batch != null) {
            if (cachedBatch == null || !cachedBatch.hasBatchId(batch.getBatchId())) {
                clearCachedPassword();
                cachedBatch = batch;
            }

            if (cachedPassword == null) {
                cachedPassword = ui.getContextSpecificPassword();
            }
            return cachedPassword;
        }

        return ui.getContextSpecificPassword();
    }

    public boolean isCachingPIN() {
        return currentBatch.get() != null;
    }

    public void withoutCachedPIN(Runnable operation) {
        withBatchContext(null, operation);
    }

    public void withCachedPIN(Batch batch, Runnable operation) {
        withBatchContext(Objects.requireNonNull(batch), operation);
    }

    private void withBatchContext(Batch batch, Runnable operation) {
        var previousBatch = currentBatch.get();
        currentBatch.set(batch);
        try {
            operation.run();
        } finally {
            if (previousBatch == null)
                currentBatch.remove();
            else
                currentBatch.set(previousBatch);
        }
    }

    public synchronized void reset() {
        clearCachedPassword();
        cachedBatch = null;
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
