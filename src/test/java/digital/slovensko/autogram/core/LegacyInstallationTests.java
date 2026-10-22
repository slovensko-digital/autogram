package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Test;

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
}
