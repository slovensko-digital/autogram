package digital.slovensko.autogram.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ApplicationRestarterTest {

    @Test
    public void testBuildRestartCommandPreservesJvmAndAppArgs() {
        var command = ApplicationRestarter.buildRestartCommand(
            "/fake/java",
            List.of("-Xmx512m", "-Dfile.encoding=UTF-8", "--add-opens", "java.base/java.security=ALL-UNNAMED"),
            "/fake/classpath",
            "digital.slovensko.autogram.Main",
            new String[] { "--source", "doc.pdf" });

        assertEquals(
            List.of("/fake/java", "-Xmx512m", "-Dfile.encoding=UTF-8", "--add-opens",
                "java.base/java.security=ALL-UNNAMED", "-cp", "/fake/classpath",
                "digital.slovensko.autogram.Main", "--source", "doc.pdf"),
            command);
    }

    @Test
    public void testBuildRestartCommandFiltersStaleClasspath() {
        var command = ApplicationRestarter.buildRestartCommand(
            "/fake/java",
            List.of("-Xmx512m", "-cp", "/stale/classpath", "-Dfoo=bar", "-classpath=/also-stale"),
            "/fake/classpath",
            "digital.slovensko.autogram.Main",
            new String[0]);

        var cpOccurrences = command.stream().filter("-cp"::equals).count();
        assertEquals(1, cpOccurrences);
        assertFalse(command.contains("/stale/classpath"));
        assertFalse(command.contains("-classpath=/also-stale"));
        assertEquals("/fake/classpath", command.get(command.indexOf("-cp") + 1));
    }
}
