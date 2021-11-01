package com.octosign.whitelabel.ui;

import java.util.Arrays;
import java.util.ResourceBundle;

public class I18n {
    private static boolean strictMode = true;

    public static String translate(String path, Object... args) {
        return getProperty(path, args);
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

        var msg = String.format("Cardinalities mismatch. %d args, %d specifiers", args.length, specifiersCount);
        if (strictMode) {
            throw new IllegalArgumentException(msg);
        } else {
            System.out.println("Warning! " + msg);
            return Arrays.copyOf(args, Math.max(specifiersCount, args.length));
        }
    }

    public static ResourceBundle getBundle() { return ResourceBundle.getBundle(Main.class.getCanonicalName().toLowerCase()); }

    public static void setDevMode(boolean devMode) { I18n.strictMode = !devMode; }
}
