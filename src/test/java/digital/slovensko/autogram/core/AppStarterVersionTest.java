package digital.slovensko.autogram.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The -V / --version option prints the version of the installed application and is listed in --help. */
class AppStarterVersionTest {
    /** Any version the installer could pass, not related to the current release. */
    private static final String INSTALLED_VERSION = "1.2.3";

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private String originalVersion;

    /** Catches what the application prints and remembers the version property, the tests change both. */
    @BeforeEach
    void captureOutput() {
        originalOut = System.out;
        originalVersion = System.getProperty("jpackage.app-version");
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    /** Puts back the console output and the version property, so other tests are not affected. */
    @AfterEach
    void restore() {
        System.setOut(originalOut);
        if (originalVersion == null)
            System.clearProperty("jpackage.app-version");
        else
            System.setProperty("jpackage.app-version", originalVersion);
    }

    /** The installer passes the version to the application as jpackage.app-version. */
    @Test
    void versionIsPrinted() {
        System.setProperty("jpackage.app-version", INSTALLED_VERSION);

        AppStarter.start(new String[] {"--version"});

        assertEquals(INSTALLED_VERSION, printed());
    }

    @Test
    void shortOptionPrintsVersionToo() {
        System.setProperty("jpackage.app-version", INSTALLED_VERSION);

        AppStarter.start(new String[] {"-V"});

        assertEquals(INSTALLED_VERSION, printed());
    }

    /** Run from sources there is no installed version. */
    @Test
    void developmentVersionIsPrintedAsDev() {
        System.clearProperty("jpackage.app-version");

        AppStarter.start(new String[] {"--version"});

        assertEquals("dev", printed());
    }

    /** Users learn about the option from the help, it must be listed there with both its short and long name. */
    @Test
    void helpListsVersionOption() {
        AppStarter.start(new String[] {"--help"});

        assertTrue(printed().contains("-V,--version"), printed());
    }

    private String printed() {
        return output.toString(StandardCharsets.UTF_8).strip();
    }
}
