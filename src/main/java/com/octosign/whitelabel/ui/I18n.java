package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.signing.OperatingSystem;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.octosign.whitelabel.signing.OperatingSystem.MAC;

public class I18n {
    private static final String PATH = I18n.class.getCanonicalName().toLowerCase();
    private static ResourceBundle BUNDLE;
    private static final Map<List<String>, Locale> MAPPING = Map.of(
            List.of("en", "uk", "us", "gb", "en-us", "en-gb", "en-uk", "en-en"), Locale.forLanguageTag("en-US.UTF-8"),
            List.of("sk", "svk", "sk-sk"), Locale.forLanguageTag("sk-SK.UTF-8")
    );

    static {
        setLocale(Locale.getDefault());
    }

    public static void setLocale(Locale locale) {
        BUNDLE = ResourceBundle.getBundle(PATH, locale);
    }

    public static void setLanguage(String language) {
        var locale = map(language);
        if (OperatingSystem.current() == MAC) {
            Locale.setDefault(locale);
            System.setProperty("user.language", language.substring(0, 2));
        }

        setLocale(locale);
    }

    private static Locale map(String tag) {
        var key = MAPPING.keySet().stream().filter(list -> list.contains(tag)).findFirst();
        if (key.isPresent())
            return MAPPING.get(key.get());
        else
            return Locale.getDefault();
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
