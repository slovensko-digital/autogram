package digital.slovensko.autogram.ui.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliHeadlessSettingsTest {
    @Test
    void parsesHeadlessCertificateAndPinOptions() throws Exception {
        var settings = CliSettings.fromCmd(parse(
                "--driver", "secure_store",
                "--key", "123456789",
                "--pin-stdin",
                "--list-keys"));

        assertEquals("secure_store", settings.getDefaultDriver());
        assertEquals("123456789", settings.getKeySelector());
        assertTrue(settings.isPinFromStdin());
        assertTrue(settings.isListKeys());
    }

    @Test
    void matchesCertificateBySerialOrCommonName() {
        assertTrue(CliKeySelector.matches("123456789", "Test Signer", "123456789"));
        assertTrue(CliKeySelector.matches("123456789", "Test Signer", "Test Signer"));
        assertFalse(CliKeySelector.matches("123456789", "Test Signer", "Someone Else"));
    }

    @Test
    void reportsUnexpectedCliFailuresWithNonZeroStatus() {
        var originalError = System.err;
        var capturedError = new ByteArrayOutputStream();
        System.setErr(new PrintStream(capturedError));
        try {
            assertEquals(1, CliApp.reportFailure(new IllegalStateException("test failure")));
        } finally {
            System.setErr(originalError);
        }
        assertTrue(capturedError.toString(StandardCharsets.UTF_8).contains("test failure"));
    }

    private static CommandLine parse(String... args) throws Exception {
        var options = new Options()
                .addOption("d", "driver", true, "")
                .addOption(null, "key", true, "")
                .addOption(null, "pin-stdin", false, "")
                .addOption(null, "list-keys", false, "");
        return new DefaultParser().parse(options, args);
    }
}
