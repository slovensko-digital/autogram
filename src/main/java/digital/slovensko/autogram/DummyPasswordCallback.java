package digital.slovensko.autogram;

import eu.europa.esig.dss.token.PasswordInputCallback;

public class DummyPasswordCallback implements PasswordInputCallback {
    @Override
    public char[] getPassword() {
        return null;
    }
}
