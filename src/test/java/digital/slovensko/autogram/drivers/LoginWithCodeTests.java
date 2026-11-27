package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.TokenCodes;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.BokIncorrectException;
import digital.slovensko.autogram.core.errors.PINLockedException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import eu.europa.esig.dss.model.DSSException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginWithCodeTests {
    private static class FakeToken implements LoginWithCode.Token {
        boolean loggedIn;
        String failure;
        final List<char[]> codes = new ArrayList<>();
        final List<String> codesWhenPassed = new ArrayList<>();

        @Override
        public boolean isLoggedIn() {
            return loggedIn;
        }

        @Override
        public void logIn(char[] code) throws LoginWithCode.TokenException {
            codes.add(code);
            codesWhenPassed.add(new String(code));
            if (failure != null)
                throw new LoginWithCode.TokenException(failure);
            loggedIn = true;
        }
    }

    private static class AskingUI extends TestAutogramFactory.FakeUI {
        final List<TokenCodes> asked = new ArrayList<>();
        final String code;

        AskingUI(String code) {
            this.code = code;
        }

        @Override
        public char[] getKeystorePassword(TokenCodes codes) {
            asked.add(codes);
            return code == null ? null : code.toCharArray();
        }
    }

    private static PasswordManager passwordManager(AskingUI ui) {
        return new PasswordManager(ui, new UserSettings());
    }

    @Test
    void testBokFromAutogramLogsIn() {
        var ui = new AskingUI("123456");
        var token = new FakeToken();

        LoginWithCode.logIn(token, passwordManager(ui), TokenCodes.EID);

        assertEquals(List.of(TokenCodes.EID), ui.asked);
        assertEquals(List.of("123456"), token.codesWhenPassed);
        // the BOK doesn't stay in memory
        assertArrayEquals(new char[6], token.codes.get(0));
    }

    @Test
    void testLoggedInTokenIsNotAskedForBok() {
        var ui = new AskingUI("123456");
        var token = new FakeToken();
        token.loggedIn = true;

        LoginWithCode.logIn(token, passwordManager(ui), TokenCodes.EID);

        assertEquals(List.of(), ui.asked);
        assertEquals(List.of(), token.codesWhenPassed);
    }

    @Test
    void testCanceledBokIsNotSent() {
        var token = new FakeToken();

        assertThrows(PasswordNotProvidedException.class, () -> LoginWithCode.logIn(token, passwordManager(new AskingUI(null)), TokenCodes.EID));
        assertEquals(List.of(), token.codesWhenPassed);
    }

    @Test
    void testWrongBokIsReported() {
        var token = new FakeToken();
        token.failure = "CKR_PIN_INCORRECT";

        assertThrows(BokIncorrectException.class, () -> LoginWithCode.logIn(token, passwordManager(new AskingUI("123456")), TokenCodes.EID));
    }

    @Test
    void testBlockedBokIsReported() {
        var token = new FakeToken();
        token.failure = "CKR_PIN_LOCKED";

        assertThrows(PINLockedException.class, () -> LoginWithCode.logIn(token, passwordManager(new AskingUI("123456")), TokenCodes.EID));
    }

    @Test
    void testOtherFailureCanBeRecoveredFrom() {
        // PKCS11TokenDriver.recoverToken connects to eID again after CKR_FUNCTION_FAILED, found as the root cause
        var token = new FakeToken();
        token.failure = "CKR_FUNCTION_FAILED";

        var e = assertThrows(DSSException.class, () -> LoginWithCode.logIn(token, passwordManager(new AskingUI("123456")), TokenCodes.EID));
        Throwable root = e;
        while (root.getCause() != null)
            root = root.getCause();
        assertEquals("CKR_FUNCTION_FAILED", root.getMessage());
    }

    @Test
    void testLoginByAnotherConnectionMeanwhileIsNoFailure() {
        var token = new FakeToken();
        token.failure = "CKR_USER_ALREADY_LOGGED_IN";

        LoginWithCode.logIn(token, passwordManager(new AskingUI("123456")), TokenCodes.EID);
    }
}
