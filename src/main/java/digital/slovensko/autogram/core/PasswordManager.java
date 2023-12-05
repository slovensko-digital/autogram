package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.PasswordInputCallback;

import java.util.Arrays;

public class PasswordManager implements PasswordInputCallback {
    private final UI ui;
    private final PasswordManagerSettings settings;
    private char[] cachedPassword;

    public PasswordManager(UI ui, PasswordManagerSettings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    public char[] getContextSpecificPassword() {
        if (settings.getCacheContextSpecificPasswordEnabled()) {
            if (cachedPassword == null) {
                cachedPassword = ui.getContextSpecificPassword();
            }
            return cachedPassword;
        } else {
            return ui.getContextSpecificPassword();
        }
    }

    public void reset() {
        if (cachedPassword != null)
            Arrays.fill(cachedPassword, '\0');

        cachedPassword = null;
    }

    @Override
    public char[] getPassword() {
        return ui.getKeystorePassword();
    }
}
