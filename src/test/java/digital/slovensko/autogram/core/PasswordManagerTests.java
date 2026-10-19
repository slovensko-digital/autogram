package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
            public char[] getKeystorePassword() {
                return "1234".toCharArray();
            }
        };
        var passwordManager = new PasswordManager(ui, new UserSettings());

        assertArrayEquals("1234".toCharArray(), passwordManager.getPassword());
    }
}
