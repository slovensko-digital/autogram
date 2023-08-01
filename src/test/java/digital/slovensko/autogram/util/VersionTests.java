package digital.slovensko.autogram.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VersionTests {

    @Test
    public void testVersionCompareTo() {
        var v1 = new Version("1.0.0");
        var v2 = new Version("1.0.1");
        var v3 = new Version("v1.1.0");
        var v4 = new Version("v2.0.0");
        var v5 = new Version("2.0.1");
        var v6 = new Version("2.1.0");
        var v7 = new Version("2.1.1");

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
        var v0 = new Version("");
        var v1 = new Version("1.0.0");
        var v2 = new Version("0.2");
        var v3 = new Version("1");

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
}