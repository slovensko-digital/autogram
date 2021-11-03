package com.octosign.whitelabel.ui;

import java.util.Base64;

public class Utils {
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }


    public static String decode(String value) {
        if (isNullOrBlank(value))
            return null;

        return new String(Base64.getDecoder().decode(value));
    }
}
