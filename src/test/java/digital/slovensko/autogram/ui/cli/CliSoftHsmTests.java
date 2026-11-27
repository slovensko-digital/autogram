package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.SoftHsmToken;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Signing from the command line with a real PKCS#11 driver, SoftHSM, whose key needs the signing PIN before each
 * signature like eID.
 */
@Tag("Pkcs11")
class CliSoftHsmTests {
    private static final Options OPTIONS = new Options()
            .addOption("c", "cli", false, "")
            .addOption("s", "source", true, "")
            .addOption("d", "driver", true, "")
            .addOption(null, "pkcs11-driver-path", true, "");

    /**
     * Answers only the SoftHSM token's codes, a prompt for any other card's code (e.g. BOK) fails the test, so no code
     * ever goes to a real card.
     */
    private static class Codes implements Function<String, char[]> {
        static final String CANCEL = "cancel";
        final List<String> prompts = new ArrayList<>();
        final Deque<String> signingPins;

        Codes(String... signingPins) {
            this.signingPins = new ArrayDeque<>(List.of(signingPins));
        }

        @Override
        public char[] apply(String prompt) {
            prompts.add(prompt);
            if (prompt.startsWith("Enter keystore password"))
                return SoftHsmToken.PIN.toCharArray();
            if (prompt.startsWith("Enter key password")) {
                var pin = signingPins.isEmpty() ? SoftHsmToken.PIN : signingPins.remove();
                // end of input, e.g. Ctrl+D
                return pin.equals(CANCEL) ? null : pin.toCharArray();
            }

            throw new AssertionError("unexpected prompt: " + prompt);
        }

        long signingPinPrompts() {
            return prompts.stream().filter(p -> p.startsWith("Enter key password")).count();
        }
    }

    private static Path softHsm() throws Exception {
        var library = SoftHsmToken.library();
        assumeTrue(library.isPresent(), "SoftHSM is not installed");
        return library.get();
    }

    private static Path documents(Path dir, String... names) throws Exception {
        var sample = Path.of(CliSoftHsmTests.class.getResource("/digital/slovensko/autogram/sample.pdf").toURI());
        var documents = Files.createDirectories(dir.resolve("documents"));
        for (var name : names)
            Files.copy(sample, documents.resolve(name));

        return documents;
    }

    private static void sign(Path source, Path library, Codes codes) throws ParseException {
        var cmd = new DefaultParser().parse(OPTIONS, new String[]{"--cli", "-s", source.toString(), "-d", "custom_pkcs11",
                "--pkcs11-driver-path", library.toString()});
        var settings = CliSettings.fromCmd(cmd);
        CliApp.sign(settings, new CliUI(settings, codes));
    }

    @Test
    void testFolderIsSignedWithOneSigningPin(@TempDir Path dir) throws Exception {
        var library = softHsm();
        var documents = documents(dir, "a.pdf", "b.pdf", "c.pdf");
        var codes = new Codes();

        sign(documents, library, codes);

        assertEquals(1, codes.signingPinPrompts(), codes.prompts.toString());
        for (var name : List.of("a_signed.pdf", "b_signed.pdf", "c_signed.pdf"))
            assertTrue(Files.isRegularFile(dir.resolve("documents_signed").resolve(name)), name);
    }

    @Test
    void testWrongSigningPinInFolderIsAskedForAgainOnce(@TempDir Path dir) throws Exception {
        var library = softHsm();
        var documents = documents(dir, "a.pdf", "b.pdf");
        var codes = new Codes("9999", SoftHsmToken.PIN);

        sign(documents, library, codes);

        assertEquals(2, codes.signingPinPrompts(), codes.prompts.toString());
        for (var name : List.of("a_signed.pdf", "b_signed.pdf"))
            assertTrue(Files.isRegularFile(dir.resolve("documents_signed").resolve(name)), name);
    }

    @Test
    void testCanceledSigningPinEndsFolderSigning(@TempDir Path dir) throws Exception {
        var library = softHsm();
        var documents = documents(dir, "a.pdf", "b.pdf");
        var codes = new Codes(Codes.CANCEL);
        var output = new ByteArrayOutputStream();
        var stdout = System.out;
        System.setOut(new PrintStream(output, true));
        try {
            sign(documents, library, codes);
        } finally {
            System.setOut(stdout);
        }

        assertEquals(1, codes.signingPinPrompts(), codes.prompts.toString());
        // the batch ended, the next document isn't even started
        assertEquals(1, output.toString().lines().filter(l -> l.startsWith("Starting signing file")).count(), output.toString());
        try (var signed = Files.list(dir.resolve("documents_signed"))) {
            assertEquals(List.of(), signed.toList());
        }
    }

    @Test
    void testSingleFileIsSignedWithOneSigningPin(@TempDir Path dir) throws Exception {
        var library = softHsm();
        var documents = documents(dir, "a.pdf");
        var codes = new Codes();

        sign(documents.resolve("a.pdf"), library, codes);

        assertEquals(1, codes.signingPinPrompts(), codes.prompts.toString());
        assertTrue(Files.isRegularFile(documents.resolve("a_signed.pdf")));
    }
}
