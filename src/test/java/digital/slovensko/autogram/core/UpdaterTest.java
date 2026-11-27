package digital.slovensko.autogram.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdaterTest {
    @AfterEach
    void clearUpdateChannel() {
        System.clearProperty("autogram.updateChannel");
    }

    @Test
    void installedFromAurWhenTheArchPackageSaysSo() {
        // set by package.sh in the launcher of the Arch Linux build
        System.setProperty("autogram.updateChannel", "aur");

        assertTrue(Updater.isInstalledFromAur());
    }

    @Test
    void notInstalledFromAurByDefault() {
        assertFalse(Updater.isInstalledFromAur());
    }
}
