package digital.slovensko.autogram.ui;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;

public enum SupportedLanguage {
    SLOVAK(new Locale("sk", "SK")),
    ENGLISH(Locale.ENGLISH);

    public static final SupportedLanguage DEFAULT = SLOVAK;
    private final Locale locale;

    SupportedLanguage(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return locale.getDisplayLanguage(locale);
    }

    /**
     * Try to find the enum containing the same language as the provided locale.
     *
     * @param locale locale specifying the language to search for
     * @return found enum, or null if not found
     */
    @Nullable
    public static SupportedLanguage getByLocale(Locale locale) {
        if (locale == null) return null;
        return getByLanguage(locale.getLanguage());
    }

    /**
     * Try to find the enum containing the same language as the provided language.
     *
     * @param language language to search for
     * @return found enum, or null if not found
     */
    @Nullable
    public static SupportedLanguage getByLanguage(String language) {
        for (SupportedLanguage item : values()) {
            if (item.locale.getLanguage().equals(language)) {
                return item;
            }
        }

        return null;
    }
}
