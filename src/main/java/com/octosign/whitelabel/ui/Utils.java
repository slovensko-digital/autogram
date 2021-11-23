package com.octosign.whitelabel.ui;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean notNullOrBlank(String value) {
        return !(isNullOrBlank(value));
    }


    public static String encodeBase64(String value) {
        if (isNullOrBlank(value))
            return null;

        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64(String value) {
        if (isNullOrBlank(value))
            return null;

        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
