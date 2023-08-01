package digital.slovensko.autogram.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VersionTests {

    @Test
    public void testVersionCompareTo() {
        var v1 = Version.createFromVersionString("1.0.0");
        var v2 = Version.createFromVersionString("1.0.1");
        var v3 = Version.createFromVersionString("v1.1.0");
        var v4 = Version.createFromVersionString("v2.0.0");
        var v5 = Version.createFromVersionString("2.0.1");
        var v6 = Version.createFromVersionString("2.1.0");
        var v7 = Version.createFromVersionString("2.1.1");

        assertTrue(v1.compareTo(v1) == 0);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v1.compareTo(v3) < 0);
        assertTrue(v1.compareTo(v4) < 0);
        assertTrue(v1.compareTo(v5) < 0);
        assertTrue(v1.compareTo(v6) < 0);
        assertTrue(v1.compareTo(v7) < 0);

        assertTrue(v2.compareTo(v1) > 0);
        assertTrue(v2.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v3) < 0);
        assertTrue(v2.compareTo(v4) < 0);
        assertTrue(v2.compareTo(v5) < 0);
        assertTrue(v2.compareTo(v6) < 0);
        assertTrue(v2.compareTo(v7) < 0);

        assertTrue(v3.compareTo(v1) > 0);
        assertTrue(v3.compareTo(v2) > 0);
        assertTrue(v3.compareTo(v3) == 0);
        assertTrue(v3.compareTo(v4) < 0);
        assertTrue(v3.compareTo(v5) < 0);
        assertTrue(v3.compareTo(v6) < 0);
        assertTrue(v3.compareTo(v7) < 0);

        assertTrue(v4.compareTo(v1) > 0);
        assertTrue(v4.compareTo(v2) > 0);
        assertTrue(v4.compareTo(v3) > 0);
        assertTrue(v4.compareTo(v4) == 0);
        assertTrue(v4.compareTo(v5) < 0);
        assertTrue(v4.compareTo(v6) < 0);
        assertTrue(v4.compareTo(v7) < 0);

        assertTrue(v5.compareTo(v1) > 0);
        assertTrue(v5.compareTo(v2) > 0);
        assertTrue(v5.compareTo(v3) > 0);
        assertTrue(v5.compareTo(v4) > 0);
        assertTrue(v5.compareTo(v5) == 0);
        assertTrue(v5.compareTo(v6) < 0);
        assertTrue(v5.compareTo(v7) < 0);

        assertTrue(v6.compareTo(v1) > 0);
        assertTrue(v6.compareTo(v2) > 0);
        assertTrue(v6.compareTo(v3) > 0);
        assertTrue(v6.compareTo(v4) > 0);
        assertTrue(v6.compareTo(v5) > 0);
        assertTrue(v6.compareTo(v6) == 0);
        assertTrue(v6.compareTo(v7) < 0);

        assertTrue(v7.compareTo(v1) > 0);
        assertTrue(v7.compareTo(v2) > 0);
        assertTrue(v7.compareTo(v3) > 0);
        assertTrue(v7.compareTo(v4) > 0);
        assertTrue(v7.compareTo(v5) > 0);
        assertTrue(v7.compareTo(v6) > 0);
        assertTrue(v7.compareTo(v7) == 0);
    }

    @Test
    public void testVersionCompareToWithDifferentLength() {
        var v0 = Version.createFromVersionString("");
        var v1 = Version.createFromVersionString("1.0.0");
        var v2 = Version.createFromVersionString("0.2");
        var v3 = Version.createFromVersionString("1");

        assertTrue(v0.compareTo(v0) == 0);
        assertTrue(v0.compareTo(v1) < 0);
        assertTrue(v0.compareTo(v2) < 0);
        assertTrue(v0.compareTo(v3) < 0);

        assertTrue(v1.compareTo(v0) > 0);
        assertTrue(v1.compareTo(v1) == 0);
        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v1.compareTo(v3) > 0);

        assertTrue(v2.compareTo(v0) > 0);
        assertTrue(v2.compareTo(v1) < 0);
        assertTrue(v2.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v3) < 0);

        assertTrue(v3.compareTo(v0) > 0);
        assertTrue(v3.compareTo(v1) < 0);
        assertTrue(v3.compareTo(v2) > 0);
        assertTrue(v3.compareTo(v3) == 0);
    }

    @Test
    public void testDev(){
        var dev = Version.createFromVersionString("dev");
        var v1 = Version.createFromVersionString("1.0.0");
        var v2 = Version.createFromVersionString("0.2");

        assertTrue(dev.compareTo(dev) == 0);
        assertTrue(dev.compareTo(v1) > 0);
        assertTrue(dev.compareTo(v2) > 0);
        assertTrue(v1.compareTo(dev) < 0);
        assertTrue(v2.compareTo(dev) < 0);
    }
}