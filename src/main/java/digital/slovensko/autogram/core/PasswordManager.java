package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
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

    public char[] getContextSpecificPassword() {
        return getContextSpecificPassword(TokenCodes.ANY);
    }

    public synchronized char[] getContextSpecificPassword(TokenCodes codes) {
        contextSpecificPasswordRequested.set(true);
        var batch = currentBatch.get();
        var canReturnToSigning = !batch.isPresent() || batch.isInteractive();
        if (batch.isPresent()) {
            if (cachedBatch != batch) {
                clearCachedPassword();
                cachedBatch = batch;
            }
            if (cachedPassword == null)
                cachedPassword = askForSigningPin(canReturnToSigning, codes);
            incorrectPIN = false;
            return cachedPassword;
        }

        if (cachedBatch.isPresent()) {
            clearCachedPassword();
            cachedBatch = new NoBatch();
        }
        if (settings.getCacheContextSpecificPasswordEnabled()) {
            if (cachedPassword == null) {
                cachedPassword = askForSigningPin(canReturnToSigning, codes);
            }
            incorrectPIN = false;
            return cachedPassword;
        } else {
            var password = askForSigningPin(canReturnToSigning, codes);
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

    private char[] askForSigningPin(boolean canReturnToSigning, TokenCodes codes) {
        var pin = ui.getContextSpecificPassword(incorrectPIN, canReturnToSigning, codes);
        if (pin != null)
            requireAllowed(pin, codes);

        return pin;
    }

    @Override
    public char[] getPassword() {
        return getPassword(TokenCodes.ANY);
    }

    /**
     * Asks for the code to log in to the token: keystore password, or BOK for eID.
     *
     * @throws PasswordNotProvidedException if the user canceled entering it - PKCS#11 drivers would log in without PIN
     * otherwise, and some ask for it themselves then, e.g. MONET+ ProID+Q
     */
    public char[] getPassword(TokenCodes codes) {
        var password = ui.getKeystorePassword(codes);
        if (password == null)
            throw new PasswordNotProvidedException();

        requireAllowed(password, codes);
        return password;
    }

    /**
     * The UI checks the code already, this only makes sure a code breaking the rules never reaches the token, where
     * it would count as a wrong one.
     */
    private static void requireAllowed(char[] code, TokenCodes codes) {
        if (codes.check(code).isEmpty())
            return;

        Arrays.fill(code, '\0');
        throw new PasswordNotProvidedException();
    }
}
