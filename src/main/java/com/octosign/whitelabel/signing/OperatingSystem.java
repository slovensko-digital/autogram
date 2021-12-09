package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;

import java.util.Locale;

import static com.octosign.whitelabel.ui.I18n.translate;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MAC;

    public static OperatingSystem fromString(String osName) {
        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return MAC;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("nux")) {
            return LINUX;
        } else {
            throw new UserException("error.unknownOS.header", translate("error.unknownOS.description", osName));
        }
    }

    public static OperatingSystem current() {
        return fromString(getCurrentOsName());
    }

    private static String getCurrentOsName() {
        return System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    }
}
