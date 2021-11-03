package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {

    static { Locale.setDefault(new Locale("sk"));  }

    private static boolean strictMode = true;

    public static String translate(String path, Object... args) {
        try {
            return getProperty(path, args);
        }
        catch (Exception e) { throw new IntegrationException(Code.TRANSLATION_ERROR, e); }
    }

    public static String getProperty(String path, Object... args) {
        var message = getBundle().getString(path);
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

    public static ResourceBundle getBundle() { return ResourceBundle.getBundle(Main.class.getCanonicalName().toLowerCase()); }

    public static void setDevMode(boolean devMode) { I18n.strictMode = !devMode; }
}
