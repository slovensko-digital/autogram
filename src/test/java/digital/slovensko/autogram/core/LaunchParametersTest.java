package digital.slovensko.autogram.core;

import javafx.application.Application.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LaunchParametersTest {
    @Test
    void filesFromKeepsExistingFilesInOrderAndDropsEverythingElse(@TempDir Path tempDir) throws IOException {
        var first = Files.createFile(tempDir.resolve("first.pdf")).toFile();
        var second = Files.createFile(tempDir.resolve("second.asice")).toFile();

        var files = LaunchParameters.filesFrom(List.of(
                "autogram://localhost:37200",
                first.getPath(),
                "/nonexistent/path.pdf",
                "--pdfa",
                tempDir.toString(),
                second.getPath()));

        assertEquals(List.of(first, second), files);
    }

    @Test
    void filesFromDecodesFileUris(@TempDir Path tempDir) throws IOException {
        var path = Files.createFile(tempDir.resolve("my file.pdf"));

        var files = LaunchParameters.filesFrom(List.of(path.toUri().toString()));

        assertEquals(List.of(path.toFile()), files);
    }

    @Test
    void isAutogramUrlOnlyMatchesTheScheme() {
        assertTrue(LaunchParameters.isAutogramUrl("autogram://localhost"));
        assertFalse(LaunchParameters.isAutogramUrl("http://localhost"));
        assertFalse(LaunchParameters.isAutogramUrl("/tmp/autogram://x"));
    }

    @Test
    void fromParametersReadsUrlFromUnnamedArgument() {
        var params = LaunchParameters.fromParameters(parameters(Map.of(), List.of("autogram://localhost?port=1234")));

        assertFalse(params.isStandaloneMode());
        assertEquals(1234, params.getPort());
    }

    @Test
    void fromParametersPrefersNamedUrl() {
        var params = LaunchParameters.fromParameters(parameters(
                Map.of("url", "http://localhost?port=4321"),
                List.of("autogram://localhost?port=1234")));

        assertFalse(params.isStandaloneMode());
        assertEquals(4321, params.getPort());
    }

    @Test
    void fromParametersIsStandaloneWithoutAnyUrl(@TempDir Path tempDir) throws IOException {
        var file = Files.createFile(tempDir.resolve("file.pdf")).toFile();

        var params = LaunchParameters.fromParameters(parameters(Map.of(), List.of(file.getPath())));

        assertTrue(params.isStandaloneMode());
        assertEquals(List.of(file), params.getFiles());
    }

    private static Parameters parameters(Map<String, String> named, List<String> unnamed) {
        return new Parameters() {
            @Override
            public List<String> getRaw() {
                return Stream.concat(
                        named.entrySet().stream().map(e -> "--" + e.getKey() + "=" + e.getValue()),
                        unnamed.stream()).toList();
            }

            @Override
            public List<String> getUnnamed() {
                return unnamed;
            }

            @Override
            public Map<String, String> getNamed() {
                return named;
            }
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "localhost",
            "my-custom-local-host",
            "loopback.autogram.slovensko.digital"
    })
        void validateHostAcceptsDotlessOrExplicitLoopbackHost(String host) {
        Assertions.assertEquals(host, LaunchParameters.Validations.validateHost(host));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1",
            "localhost:37200",
            "localhost/path",
            ""
    })
    void validateHostRejectsHostsWithPortsOrPaths(String host) {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LaunchParameters.Validations.validateHost(host));
    }
}