package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.PasswordInputCallback;

import java.util.Arrays;

public class PasswordManager implements PasswordInputCallback {
    private final UI ui;
    private final PasswordManagerSettings settings;
    private char[] cachedPassword;
    private boolean batchCachingEnabled;

    public PasswordManager(UI ui, PasswordManagerSettings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    public char[] getContextSpecificPassword() {
        if (batchCachingEnabled || settings.getCacheContextSpecificPasswordEnabled()) {
            if (cachedPassword == null) {
                cachedPassword = ui.getContextSpecificPassword();
            }
            return cachedPassword;
        } else {
            return ui.getContextSpecificPassword();
        }
    }

    public void enableBatchCaching() {
        batchCachingEnabled = true;
    }

    public boolean isBatchCachingEnabled() {
        return batchCachingEnabled;
    }

    public void reset() {
        if (cachedPassword != null)
            Arrays.fill(cachedPassword, '\0');

        cachedPassword = null;
        batchCachingEnabled = false;
    }

    @Override
    public char[] getPassword() {
        return ui.getKeystorePassword();
    }
}
