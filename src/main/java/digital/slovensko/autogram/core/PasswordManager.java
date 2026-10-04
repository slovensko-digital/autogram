package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.PasswordInputCallback;

import java.util.Arrays;

public class PasswordManager implements PasswordInputCallback {
    private final UI ui;
    private final PasswordManagerSettings settings;
    private char[] cachedPassword;
    private boolean incorrectPIN;
    private Batch cachedBatch = new NoBatch();
    private final ThreadLocal<Batch> currentBatch = ThreadLocal.withInitial(NoBatch::new);
    private final ThreadLocal<Boolean> contextSpecificPasswordRequested = ThreadLocal.withInitial(() -> false);

    public PasswordManager(UI ui, PasswordManagerSettings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    public synchronized char[] getContextSpecificPassword() {
        contextSpecificPasswordRequested.set(true);
        var batch = currentBatch.get();
        var canReturnToSigning = !batch.isPresent() || batch.isInteractive();
        if (batch.isPresent()) {
            if (cachedBatch != batch) {
                clearCachedPassword();
                cachedBatch = batch;
            }
            if (cachedPassword == null)
                cachedPassword = ui.getContextSpecificPassword(incorrectPIN, canReturnToSigning);
            incorrectPIN = false;
            return cachedPassword;
        }

        if (cachedBatch.isPresent()) {
            clearCachedPassword();
            cachedBatch = new NoBatch();
        }
        if (settings.getCacheContextSpecificPasswordEnabled()) {
            if (cachedPassword == null) {
                cachedPassword = ui.getContextSpecificPassword(incorrectPIN, canReturnToSigning);
            }
            incorrectPIN = false;
            return cachedPassword;
        } else {
            var password = ui.getContextSpecificPassword(incorrectPIN, canReturnToSigning);
            incorrectPIN = false;
            return password;
        }
    }

    public synchronized void reset() {
        clearCachedPassword();
        cachedBatch = new NoBatch();
        incorrectPIN = false;
    }

    public synchronized void preparePINRetry() {
        reset();
        incorrectPIN = true;
    }

    /** Starts tracking whether the current thread's signing attempt asks for the context-specific PIN. */
    public void startSigningAttempt() {
        contextSpecificPasswordRequested.set(false);
    }

    public boolean wasContextSpecificPasswordRequested() {
        return contextSpecificPasswordRequested.get();
    }

    public Batch setBatchContext(Batch batch) {
        var previousBatch = currentBatch.get();
        currentBatch.set(batch);
        return previousBatch;
    }

    public boolean isCachingPIN() {
        return currentBatch.get().isPresent();
    }

    private synchronized void clearCachedPassword() {
        if (cachedPassword != null)
            Arrays.fill(cachedPassword, '\0');
        cachedPassword = null;
    }

    @Override
    public char[] getPassword() {
        return ui.getKeystorePassword();
    }
}
