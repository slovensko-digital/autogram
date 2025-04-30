package digital.slovensko.autogram.ui.gui;

import org.checkerframework.checker.propkey.qual.PropertyKey;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public interface HasI18n {
    ResourceBundle getResources();

    default String i18n(@PropertyKey String key) {
        if (key == null) {
            return "null";
        }

        try {
            return getResources().getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    default String translate(String key) {
        return i18n(key);
    }

    default String tr(String key) {
        return translate(key);
    }

}
