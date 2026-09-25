package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordManagerTests {

    @Test
    void rejectedPINIsPassedToTheNextContextPasswordPromptOnlyOnce() {
        var ui = mock(UI.class);
        when(ui.getContextSpecificPassword(any())).thenReturn("1234".toCharArray());
        var passwordManager = new PasswordManager(ui);
        var error = new PINIncorrectException();

        passwordManager.onContextSpecificPasswordRejected(error);
        passwordManager.withoutCachedPIN(() -> passwordManager.getContextSpecificPassword());
        passwordManager.withoutCachedPIN(() -> passwordManager.getContextSpecificPassword());

        verify(ui).getContextSpecificPassword(error);
        verify(ui).getContextSpecificPassword(null);
    }

    @Test
    void resetDiscardsPendingPINError() {
        var ui = mock(UI.class);
        when(ui.getContextSpecificPassword(any())).thenReturn("1234".toCharArray());
        var passwordManager = new PasswordManager(ui);

        passwordManager.onContextSpecificPasswordRejected(new PINIncorrectException());
        passwordManager.reset();
        passwordManager.withoutCachedPIN(() -> passwordManager.getContextSpecificPassword());

        verify(ui).getContextSpecificPassword(null);
    }
}
