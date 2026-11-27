package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.TokenCodes;
import digital.slovensko.autogram.core.errors.BokIncorrectException;
import digital.slovensko.autogram.core.errors.PINLockedException;
import eu.europa.esig.dss.model.DSSException;

import java.util.Arrays;

/**
 * Logs in to a token with the code Autogram asks for, although the token reports a protected authentication path.
 * SunPKCS11 would log in without a code then, and eID klient would ask for BOK in its own window, which can't be used
 * from the command line nor in the Mac App Store sandbox. eID klient uses the code it gets and opens no window.
 */
class LoginWithCode {
    interface Token {
        boolean isLoggedIn() throws TokenException;

        void logIn(char[] code) throws TokenException;
    }

    /**
     * Failure of the token, e.g. PKCS#11 error code such as CKR_PIN_INCORRECT as the message.
     */
    static class TokenException extends Exception {
        TokenException(String message) {
            super(message);
        }

        TokenException(Throwable cause) {
            super(cause.getMessage(), cause);
        }
    }

    static void logIn(Token token, PasswordManager passwordManager, TokenCodes codes) {
        try {
            if (token.isLoggedIn())
                return;
        } catch (TokenException e) {
            throw new DSSException("Unable to check login: " + e.getMessage(), e);
        }

        var code = passwordManager.getPassword(codes);
        try {
            token.logIn(code);
        } catch (TokenException e) {
            switch (e.getMessage()) {
                case "CKR_USER_ALREADY_LOGGED_IN" -> {
                }
                case "CKR_PIN_INCORRECT" -> throw new BokIncorrectException();
                case "CKR_PIN_LOCKED" -> throw new PINLockedException();
                // e.g. CKR_FUNCTION_FAILED, PKCS11TokenDriver.recoverToken finds it as the root cause
                default -> throw new DSSException("Unable to log in: " + e.getMessage(), e);
            }
        } finally {
            Arrays.fill(code, '\0');
        }
    }
}
