package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.GUIApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class AppStarterTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "--cli",
            "--help",
            "--usage",
            "--url=http://localhost:32700",
            "-s",
            "-t"
    })
    void testPassesRegularArgumentsThroughUnchanged(String arg) {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{arg});

            assertArrayEquals(new String[]{arg}, resolved);
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void testPassesNonexistentPathThroughUnchanged() {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var path = "/nonexistent/path/to/file.pdf";
            var resolved = AppStarter.resolveArgs(new String[]{path});

            assertArrayEquals(new String[]{path}, resolved);
            mocked.verifyNoInteractions();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "autogram://localhost:32700",
            "autogram://autogram.slovensko.digital/sign",
            "autogram://localhost:32700?path=/tmp/file.pdf"
    })
    void testConvertsAutogramUrlToUrlArgument(String url) {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{url});

            assertArrayEquals(new String[]{"--url=" + url}, resolved);
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void testOpensExistingFilePassedAsArgument(@TempDir Path tempDir) throws IOException {
        var file = Files.createFile(tempDir.resolve("file.pdf"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{file.toString()});

            assertArrayEquals(new String[0], resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of(file.toString())));
        }
    }

    @Test
    void testOpensFileUriWithStrippedPrefix() {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{"file:///tmp/file.pdf"});

            assertArrayEquals(new String[0], resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of("/tmp/file.pdf")));
        }
    }

    @Test
    void testExtractsFilesAndPreservesOtherArguments(@TempDir Path tempDir) throws IOException {
        var file = Files.createFile(tempDir.resolve("file.pdf"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{
                    "--cli",
                    file.toString(),
                    "autogram://localhost:32700"
            });

            assertArrayEquals(new String[]{"--cli", "--url=autogram://localhost:32700"}, resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of(file.toString())));
        }
    }

    @Test
    void testOpensMultipleFilesInOrder(@TempDir Path tempDir) throws IOException {
        var first = Files.createFile(tempDir.resolve("first.pdf"));
        var second = Files.createFile(tempDir.resolve("second.asice"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{first.toString(), second.toString()});

            assertArrayEquals(new String[0], resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of(first.toString(), second.toString())));
        }
    }

    @Test
    void testReturnsEmptyForEmptyArguments() {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[0]);

            assertEquals(0, resolved.length);
            mocked.verifyNoInteractions();
        }
    }
}
