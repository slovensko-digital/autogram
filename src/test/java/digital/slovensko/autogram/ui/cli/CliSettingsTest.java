package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.errors.MultipleSourcesException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CliSettingsTest {
    private static final Options OPTIONS = new Options().addOption("s", "source", true, "")
            .addOption(null, "proxy", true, "");

    @Test
    void proxyOptionIsValidatedBeforeSourceProcessing() throws ParseException {
        var cmd = parse("--proxy", "http://localhost", "nonexistent.pdf");
        assertThrows(IllegalArgumentException.class, () -> CliSettings.fromCmd(cmd));
    }

    @Test
    void proxyOptionIsIndependentAndOptional() throws ParseException {
        assertEquals("", CliSettings.fromCmd(parse()).getProxyUrl());
        assertEquals("http://[::1]:3128", CliSettings.fromCmd(parse("--proxy", "http://[::1]:3128")).getProxyUrl());
    }

    @Test
    void resolveSourcePathUsesNamedOption() throws ParseException {
        assertEquals("a.pdf", CliSettings.resolveSourcePath(parse("-s", "a.pdf")));
    }

    @Test
    void resolveSourcePathUsesSinglePositionalArgument() throws ParseException {
        assertEquals("a.pdf", CliSettings.resolveSourcePath(parse("a.pdf")));
    }

    @Test
    void resolveSourcePathIsNullWithoutSource() throws ParseException {
        assertNull(CliSettings.resolveSourcePath(parse()));
    }

    @Test
    void resolveSourcePathRejectsNamedOptionCombinedWithPositional() throws ParseException {
        var cmd = parse("-s", "a.pdf", "b.pdf");
        assertThrows(MultipleSourcesException.class, () -> CliSettings.resolveSourcePath(cmd));
    }

    @Test
    void resolveSourcePathRejectsMultiplePositionals() throws ParseException {
        var cmd = parse("a.pdf", "b.pdf");
        assertThrows(MultipleSourcesException.class, () -> CliSettings.resolveSourcePath(cmd));
    }

    private static CommandLine parse(String... args) throws ParseException {
        return new DefaultParser().parse(OPTIONS, args);
    }
}
