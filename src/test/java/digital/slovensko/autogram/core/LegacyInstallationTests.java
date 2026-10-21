package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyInstallationTests {
    @Test
    void successAndAlreadyRemovedProductCountAsUninstalled() {
        assertTrue(LegacyInstallation.isUninstalled(0));
        assertTrue(LegacyInstallation.isUninstalled(3010)); // reboot required
        assertTrue(LegacyInstallation.isUninstalled(1605)); // product no longer installed
    }

    @Test
    void canceledOrFailedUninstallIsReported() {
        assertFalse(LegacyInstallation.isUninstalled(1602)); // canceled by user
        assertFalse(LegacyInstallation.isUninstalled(1603)); // fatal error
        assertFalse(LegacyInstallation.isUninstalled(1618)); // another installation in progress
    }

    @Test
    void nothingIsSearchedOutsideMsixPackage() {
        assertTrue(LegacyInstallation.findWhenRunningFromMsix().isEmpty());
    }
}
