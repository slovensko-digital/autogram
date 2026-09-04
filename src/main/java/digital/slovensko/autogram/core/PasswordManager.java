package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.PasswordInputCallback;

import java.util.Arrays;

public class PasswordManager implements PasswordInputCallback {
    private final UI ui;
    private char[] cachedPassword;

    public PasswordManager(UI ui) {
        this.ui = ui;
    }

    public char[] getContextSpecificPassword() {
        if (cachedPassword == null) {
            cachedPassword = ui.getContextSpecificPassword();
        }
        return cachedPassword;
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
