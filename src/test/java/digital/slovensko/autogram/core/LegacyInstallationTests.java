package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyInstallationTests {
    @Test
    void successAndAlreadyRemovedProductCountAsUninstalled() {
        assertTrue(LegacyInstallation.isUninstalled(0));
        assertTrue(LegacyInstallation.isUninstalled(3010));
        assertTrue(LegacyInstallation.isUninstalled(1605));
    }

    @Test
    void canceledOrFailedUninstallIsReported() {
        assertFalse(LegacyInstallation.isUninstalled(1602));
        assertFalse(LegacyInstallation.isUninstalled(1603));
        assertFalse(LegacyInstallation.isUninstalled(1618));
    }

    @Test
    void nothingIsSearchedOutsideMsixPackage() {
        assertTrue(LegacyInstallation.findWhenRunningFromMsix().isEmpty());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void onlyAppsFromInstallLocationAreStopped() {
        var installation = new LegacyInstallation("{00000000-0000-0000-0000-000000000000}", "2.7.5", "C:\\Program Files\\Autogram\\");

        assertTrue(installation.contains("C:\\Program Files\\Autogram\\Autogram.exe"));
        assertTrue(installation.contains("c:\\program files\\autogram\\autogram-cli.exe"));
        assertFalse(installation.contains("C:\\Program Files\\WindowsApps\\SlubySlovensko.Digitals.r.Autogram_2.8.0.0_x64__y5yh3gtpad4se\\Autogram.exe"));
        assertFalse(installation.contains("C:\\Program Files\\Autogram2\\Autogram.exe"));
    }

    @Test
    void nothingIsStoppedWithoutInstallLocation() {
        var installation = new LegacyInstallation("{00000000-0000-0000-0000-000000000000}", "2.7.5", "");

        assertFalse(installation.contains("C:\\Program Files\\Autogram\\Autogram.exe"));
    }
}
