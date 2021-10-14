package com.octosign.whitelabel.ui;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

public class I18n {
    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();
    protected static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

//    private I18n() { throw new AssertionError(); }

    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
        return String.format(bundle.getString(path), args);
    }

//    public static boolean isSupported(Locale locale) { return asList(Locale.getAvailableLocales()).contains(locale); }
//    public static void setLocale(Locale locale) { Locale.setDefault(locale); }
//    public static Locale getLocale() { return Locale.getDefault(); }

//    public static ResourceBundle getBundle() { return bundle; }

}
