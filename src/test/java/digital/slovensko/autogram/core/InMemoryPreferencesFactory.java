package digital.slovensko.autogram.core;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * Preferences that live only in the memory of the process, set by -Djava.util.prefs.PreferencesFactory.
 * Unlike -Djava.util.prefs.userRoot, which only Linux honors, this keeps tests away from the user's real settings
 * on macOS (plist) and Windows (registry) too.
 */
public class InMemoryPreferencesFactory implements PreferencesFactory {
    private static final Preferences USER_ROOT = new InMemoryPreferences(null, "");
    private static final Preferences SYSTEM_ROOT = new InMemoryPreferences(null, "");

    @Override
    public Preferences userRoot() {
        return USER_ROOT;
    }

    @Override
    public Preferences systemRoot() {
        return SYSTEM_ROOT;
    }

    private static class InMemoryPreferences extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();
        private final Map<String, InMemoryPreferences> children = new HashMap<>();

        InMemoryPreferences(InMemoryPreferences parent, String name) {
            super(parent, name);
        }

        @Override
        protected void putSpi(String key, String value) {
            values.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return values.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            values.remove(key);
        }

        @Override
        protected void removeNodeSpi() {
            ((InMemoryPreferences) parent()).children.remove(name());
        }

        @Override
        protected String[] keysSpi() {
            return values.keySet().toArray(String[]::new);
        }

        @Override
        protected String[] childrenNamesSpi() {
            return children.keySet().toArray(String[]::new);
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return children.computeIfAbsent(name, childName -> new InMemoryPreferences(this, childName));
        }

        @Override
        protected void syncSpi() { }

        @Override
        protected void flushSpi() { }
    }
}
