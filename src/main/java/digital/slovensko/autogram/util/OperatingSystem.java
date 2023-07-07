package digital.slovensko.autogram.util;

import java.util.Locale;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MAC;

    public static OperatingSystem current() {
        var osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return MAC;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("nux")) {
            return LINUX;
        } else {
            throw new RuntimeException("Unsupported OS");
        }
    }
}
