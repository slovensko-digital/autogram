package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserSettingsTest {

    @TempDir
    Path tempDir;

    @Test
    public void testGetLastUsedDirectoryEmptyByDefault() {
        var settings = new UserSettings();

        assertTrue(settings.getLastUsedDirectory().isEmpty());
    }

    @Test
    public void testSetAndGetLastUsedDirectoryWithValidDirectory() {
        var settings = new UserSettings();

        settings.setLastUsedDirectory(tempDir);

        assertEquals(tempDir.toString(), settings.getLastUsedDirectory().orElseThrow());
    }

    @Test
    public void testSetLastUsedDirectoryWithNullClearsIt() {
        var settings = new UserSettings();
        settings.setLastUsedDirectory(tempDir);

        settings.setLastUsedDirectory((Path) null);

        assertTrue(settings.getLastUsedDirectory().isEmpty());
    }

    @Test
    public void testSetLastUsedDirectoryIgnoresRegularFile() throws IOException {
        var settings = new UserSettings();
        var file = Files.createFile(tempDir.resolve("not-a-directory.txt"));

        settings.setLastUsedDirectory(file);

        assertTrue(settings.getLastUsedDirectory().isEmpty());
    }

    @Test
    public void testSetLastUsedDirectoryIgnoresNonExistentPath() {
        var settings = new UserSettings();
        var missing = tempDir.resolve("does-not-exist");

        settings.setLastUsedDirectory(missing);

        assertTrue(settings.getLastUsedDirectory().isEmpty());
    }

    @Test
    public void testGetLastUsedDirectoryReturnsEmptyWhenDirectoryRemovedWhileRunning() throws IOException {
        var settings = new UserSettings();
        var removable = Files.createDirectory(tempDir.resolve("removable"));
        settings.setLastUsedDirectory(removable);
        assertTrue(settings.getLastUsedDirectory().isPresent());

        Files.delete(removable);

        assertTrue(settings.getLastUsedDirectory().isEmpty());
    }

    @Test
    public void testSetLastUsedDirectoryFromStringRoundTrips() {
        var settings = new UserSettings();

        settings.setLastUsedDirectory(tempDir.toString());

        assertEquals(tempDir.toString(), settings.getLastUsedDirectory().orElseThrow());
    }

    @Test
    public void testSetLastUsedDirectoryFromBlankStringClearsIt() {
        var settings = new UserSettings();
        settings.setLastUsedDirectory(tempDir);

        settings.setLastUsedDirectory("   ");

        assertTrue(settings.getLastUsedDirectory().isEmpty());
    }
}
