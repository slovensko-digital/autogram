package com.octosign.whitelabel.ui;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static final String PATH = I18n.class.getCanonicalName().toLowerCase();
    private static ResourceBundle BUNDLE;

    private static boolean isInitialized = false;

    public static void setLocale(Locale locale) {
        if (isInitialized)
            throw new RuntimeException("Unable to reinitialize ResourceBundle");

        BUNDLE = ResourceBundle.getBundle(PATH, locale);
        isInitialized = true;
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

        if (specifiersCount == args.length) {
            return args;
        } else {
            var message = "Cardinality mismatch: " + args.length + " args, " + specifiersCount + " specifiers";
            throw new IllegalArgumentException(message);
        }
    }

    public static Locale getLocale() {
        return BUNDLE.getLocale();
    }

    public static ResourceBundle getBundle() {
        return BUNDLE;
    }
}
