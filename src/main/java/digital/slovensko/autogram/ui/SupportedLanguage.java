package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.UserSettings;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

public enum SupportedLanguage {
    SLOVAK(new Locale("sk", "SK")),
    ENGLISH(Locale.ENGLISH);

    public static final SupportedLanguage SYSTEM = null;
    private final Locale locale;

    SupportedLanguage(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getDisplayLanguage() {
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

    public static ResourceBundle loadResources(UserSettings userSettings) {
        Locale language = userSettings.getLanguageLocale();
        return ResourceBundle.getBundle("digital.slovensko.autogram.ui.gui.language.l10n", language);
    }
}
