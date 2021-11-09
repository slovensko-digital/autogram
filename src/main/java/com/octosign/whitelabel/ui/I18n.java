package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {
    private static final ResourceBundle BUNDLE;

    private static boolean strictMode = true;

    static {
        var locale = new Locale("sk");
        var resourcePath = I18n.class.getCanonicalName().toLowerCase();
        BUNDLE = ResourceBundle.getBundle(resourcePath, locale);
        Locale.setDefault(locale);
    }

    public static String translate(String path, Object... args) {
        try {
            return getTranslation(path, args);
        }
        catch (MissingResourceException e) { throw new IntegrationException(Code.RESOURCE_NOT_FOUND, e); }
        catch (Exception e) { throw new IntegrationException(Code.TRANSLATION_FAILED, e); }
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

        var message = String.format("Cardinality mismatch: %d args, %d specifiers", args.length, specifiersCount);
        if (strictMode) {
            throw new IllegalArgumentException(message);
        } else {
            System.out.println("WARNING: " + message);
            return Arrays.copyOf(args, Math.max(specifiersCount, args.length));
        }
    }

    public static ResourceBundle getBundle() { return BUNDLE; }
}
