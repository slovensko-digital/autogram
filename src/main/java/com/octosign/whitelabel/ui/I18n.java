package com.octosign.whitelabel.ui;

import java.util.Arrays;
import java.util.ResourceBundle;

public class I18n {
    private static final ResourceBundle BUNDLE;

    private static boolean strictMode = true;

    static {
        var resourcePath = I18n.class.getCanonicalName().toLowerCase();
        BUNDLE = ResourceBundle.getBundle(resourcePath);
    }

    public static String translate(String path, Object... args) {
        try {
            return getTranslation(path, args);
        }
        catch (Exception e) {
            throw new RuntimeException("Translation failed for: " + path, e);
        }
    }

    private static String getTranslation(String path, Object... args) {
        var message = BUNDLE.getString(path);
        if (args == null || args.length == 0)
            return message;
        else
            return String.format(message, validateArgs(message, args));
    }

    private static Object[] validateArgs(String value, Object... args) {
        var specifiersCount = value.length() - value.replace("%", "").length();
        if (specifiersCount == args.length)
            return args;

        var message = "Cardinality mismatch: " + args.length + " args, " + specifiersCount + " specifiers";
        if (strictMode) {
            throw new IllegalArgumentException(message);
        } else {
            System.out.println("WARNING: " + message);
            return Arrays.copyOf(args, Math.max(specifiersCount, args.length));
        }
    }

    public static ResourceBundle getBundle() {
        return BUNDLE;
    }
}
