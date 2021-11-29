package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.UserException;

import java.util.Locale;

import static com.octosign.whitelabel.ui.I18n.translate;

public enum OS {
    WINDOWS,
    LINUX,
    DARWIN;

    public static OS fromString(String osName) {
        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return DARWIN;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("nux")) {
            return LINUX;
        } else {
            throw new UserException("error.unknownOS.header", translate("error.unknownOS.description", osName));
        }
    }

    public static OS current() {
        return fromString(getCurrentOsName());
    }

    private static String getCurrentOsName() {
        return System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    }
}
