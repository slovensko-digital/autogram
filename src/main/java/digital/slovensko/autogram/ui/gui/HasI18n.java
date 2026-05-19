package digital.slovensko.autogram.ui.gui;

import org.checkerframework.checker.propkey.qual.PropertyKey;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

public interface HasI18n {
    StackWalker STACK_WALKER = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);

    ResourceBundle getResources();

    default String i18n(@PropertyKey String key, Object... args) {
        return translate(getResources(), key, args);
    }

    static String translate(ResourceBundle resources, @PropertyKey String key, Object... args) {
        if (key == null) {
            return "null";
        }
        if (resources == null) {
            return key;
        }

        try {
            var template = resources.getString(key);
            return MessageFormat.format(template, args);
        } catch (MissingResourceException | IllegalArgumentException e) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass())
                    .error("Failed to translate key {} with args {}", resources, args, e);
            return key;
        }
    }

}
