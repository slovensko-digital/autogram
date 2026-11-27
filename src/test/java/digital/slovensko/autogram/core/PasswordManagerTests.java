package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordManagerTests {
    @Test
    void testCanceledPasswordIsNotPassedOn() {
        // FakeUI returns null as if the user canceled entering it
        var passwordManager = new PasswordManager(new TestAutogramFactory.FakeUI(), new UserSettings());

        assertThrows(PasswordNotProvidedException.class, passwordManager::getPassword);
    }

    @Test
    void testEnteredPasswordIsPassedOn() {
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getKeystorePassword(TokenCodes codes) {
                return "1234".toCharArray();
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertArrayEquals("1234".toCharArray(), passwordManager.getPassword());
    }

    @Test
    void testLoginCodeIsAskedForWithTheTokensRules() {
        var asked = new ArrayList<TokenCodes>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getKeystorePassword(TokenCodes codes) {
                asked.add(codes);
                return "123456".toCharArray();
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertArrayEquals("123456".toCharArray(), passwordManager.getPassword(TokenCodes.EID));
        assertEquals(List.of(TokenCodes.EID), asked);
    }

    @Test
    void testLoginCodeBreakingTheRulesIsNotPassedOn() {
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getKeystorePassword(TokenCodes codes) {
                return "12345ľ".toCharArray();
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertThrows(PasswordNotProvidedException.class, () -> passwordManager.getPassword(TokenCodes.EID));
    }

    @Test
    void testEmptyLoginCodeIsPassedOnIfTheRulesAllowIt() {
        // some tokens need no code, the user leaves the field empty
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getKeystorePassword(TokenCodes codes) {
                return new char[0];
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertArrayEquals(new char[0], passwordManager.getPassword(TokenCodes.DIGITS));
        assertThrows(PasswordNotProvidedException.class, () -> passwordManager.getPassword(TokenCodes.EID));
    }

    @Test
    void testSigningPinIsAskedForWithTheTokensRules() {
        var asked = new ArrayList<TokenCodes>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getContextSpecificPassword(boolean incorrectPIN, boolean canReturnToSigning, TokenCodes codes) {
                asked.add(codes);
                return "123456".toCharArray();
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertArrayEquals("123456".toCharArray(), passwordManager.getContextSpecificPassword(TokenCodes.EID));
        assertEquals(List.of(TokenCodes.EID), asked);
    }

    @Test
    void testSigningPinBreakingTheRulesIsNotPassedOn() {
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getContextSpecificPassword(boolean incorrectPIN, boolean canReturnToSigning, TokenCodes codes) {
                return "12345".toCharArray();
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertThrows(PasswordNotProvidedException.class, () -> passwordManager.getContextSpecificPassword(TokenCodes.EID));
    }
}
