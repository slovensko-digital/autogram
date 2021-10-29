package com.octosign.whitelabel.ui;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class I18n {
    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();
    protected static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
//        String property = bundle.getString(path);
        String replaced = path.replaceAll("/[^%[a-z]]/", "");
        int specifierCount = replaced.length() / 2;
        System.out.println(specifierCount);
        System.out.println(replaced);

        if (specifierCount != args.length) {
            throw new IllegalArgumentException("Invalid number of arguments (more or less than expected)");
        }

        return String.format(path, args);
    }

    public static String translate(String path, Object... args) {
        if (args.length == 0) return getProperty(path);
        else return getProperty(path, args);
    }

//    public static boolean isSupported(Locale locale) { return asList(Locale.getAvailableLocales()).contains(locale); }
//    public static void setLocale(Locale locale) { Locale.setDefault(locale); }
//    public static Locale getLocale() { return Locale.getDefault(); }

//    public static ResourceBundle getBundle() { return bundle; }

}
