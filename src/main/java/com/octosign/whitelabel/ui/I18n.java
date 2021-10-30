package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.IntegrationException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class I18n {

    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();

    protected static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

    // Indicates how the application behaves in case of incorrect string interpolation arguments count
    private static boolean useStrict = true;

    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
        var message = getProperty(path);
        String[] converted = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
        String[] safeArgs = validate(message, converted);

        return String.format(message, safeArgs);
    }

    private static String[] validate(String text, String... args) {
        var specifiersCount = text.length() - text.replace("%", "").length();
        var higherCount = (specifiersCount == args.length) ? null : Math.max(specifiersCount, args.length);

        if (higherCount != null) {
            var message = String.format("Numbers of input args and specifiers don't match. %d (args ) : %d (specifiers)", args.length, specifiersCount);

            if (useStrict) throw new IllegalArgumentException(message);
            else System.out.println("Warning!" + message);

            return Arrays.copyOf(args, higherCount);
        }

        return args;
    }

    public static String translate(String path, Object... args) {
        if (args.length == 0)
            return getProperty(path);
        else
            return getProperty(path, args);
    }

    public static void setDevMode(boolean devMode) { I18n.useStrict = devMode; }
}