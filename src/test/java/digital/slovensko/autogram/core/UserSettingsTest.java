package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserSettingsTest {

    @TempDir
    Path tempDir;

    private String readSavedServerEnabled() {
        return Preferences.userNodeForPackage(UserSettings.class).get("SERVER_ENABLED", null);
    }

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

    @Test
    public void testRestartRequiredSettingsChangedWhenServerToggled() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);
        var original = readSavedServerEnabled();
        try {
            prefs.putBoolean("SERVER_ENABLED", true);

            var settings = new UserSettings();
            settings.setServerEnabled(true);
            assertFalse(settings.restartRequiredSettingsChanged());

            settings.setServerEnabled(false);
            assertTrue(settings.restartRequiredSettingsChanged());

            settings.setServerEnabled(true);
            assertFalse(settings.restartRequiredSettingsChanged());
        } finally {
            restoreSavedServerEnabled(original);
        }
    }

    @Test
    public void testRestartRequiredSettingsChangedByReset() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);
        var original = readSavedServerEnabled();
        try {
            var settings = new UserSettings();

            prefs.putBoolean("SERVER_ENABLED", true); // default value
            assertFalse(settings.restartRequiredSettingsChangedByReset());

            prefs.putBoolean("SERVER_ENABLED", false); // differs from default
            assertTrue(settings.restartRequiredSettingsChangedByReset());
        } finally {
            restoreSavedServerEnabled(original);
        }
    }

    private void restoreSavedServerEnabled(String original) {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);
        if (original == null)
            prefs.remove("SERVER_ENABLED");
        else
            prefs.put("SERVER_ENABLED", original);
    }
}
