package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
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

    /**
     * @throws PasswordNotProvidedException if the user canceled entering it - PKCS#11 drivers would log in without PIN
     * otherwise, and some ask for it themselves then, e.g. MONET+ ProID+Q
     */
    @Override
    public char[] getPassword() {
        var password = ui.getKeystorePassword();
        if (password == null)
            throw new PasswordNotProvidedException();

        return password;
    }
}
