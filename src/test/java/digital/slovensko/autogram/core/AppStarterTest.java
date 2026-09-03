package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.GUIApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mockStatic;

public class AppStarterTest {
    @Test
    void testPassesNonexistentPathThroughUnchanged() {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var path = "/nonexistent/path/to/file.pdf";
            var resolved = AppStarter.resolveArgs(new String[]{path});

            assertArrayEquals(new String[]{path}, resolved);
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void testConvertsAutogramUrlToUrlArgument() {
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var url = "autogram://localhost:32700";
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
    void testOpensFileUriWithStrippedPrefix(@TempDir Path tempDir) {
        var file = tempDir.resolve("file.pdf");
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{file.toUri().toString()});

            assertArrayEquals(new String[0], resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of(file.toString())));
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
    void testKeepsExistingFileAsValueOfShortOption(@TempDir Path tempDir) throws IOException {
        var source = Files.createFile(tempDir.resolve("in.pdf"));
        var target = Files.createFile(tempDir.resolve("out.pdf"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{"--cli", "-s", source.toString(), "-t", target.toString()});

            assertArrayEquals(new String[]{"--cli", "-s", source.toString(), "-t", target.toString()}, resolved);
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void testKeepsExistingFileAsValueOfLongOption(@TempDir Path tempDir) throws IOException {
        var keystore = Files.createFile(tempDir.resolve("keys.p12"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{"--cli", "--keystore", keystore.toString()});

            assertArrayEquals(new String[]{"--cli", "--keystore", keystore.toString()}, resolved);
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void testOpensFileUriWithEncodedCharacters(@TempDir Path tempDir) throws IOException {
        var file = Files.createFile(tempDir.resolve("my file.pdf"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{file.toUri().toString()});

            assertArrayEquals(new String[0], resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of(file.toAbsolutePath().toString())));
        }
    }

    @Test
    void testOpensExistingFileNextToUrlOption(@TempDir Path tempDir) throws IOException {
        var file = Files.createFile(tempDir.resolve("file.pdf"));
        try (MockedStatic<GUIApp> mocked = mockStatic(GUIApp.class)) {
            var resolved = AppStarter.resolveArgs(new String[]{"--url=http://localhost:32700", file.toString()});

            assertArrayEquals(new String[]{"--url=http://localhost:32700"}, resolved);
            mocked.verify(() -> GUIApp.setFilesToOpen(List.of(file.toString())));
        }
    }
}
