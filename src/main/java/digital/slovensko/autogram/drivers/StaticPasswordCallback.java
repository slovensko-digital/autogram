package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.PasswordInputCallback;

public class StaticPasswordCallback implements PasswordInputCallback {
    private final char[] password;

    public StaticPasswordCallback(char[] password) {
        this.password = password;
    }

    @Override
    public char[] getPassword() {
        return password;
    }
}
