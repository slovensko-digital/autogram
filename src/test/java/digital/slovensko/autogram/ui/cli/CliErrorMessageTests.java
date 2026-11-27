package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.errors.BokIncorrectException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CliErrorMessageTests {
    @Test
    void testWrongBokIsToldApartFromWrongPin() {
        assertEquals("Incorrect BOK", CliUI.parseError(new BokIncorrectException()));
        assertEquals("Incorrect security code", CliUI.parseError(new PINIncorrectException()));
    }
}
