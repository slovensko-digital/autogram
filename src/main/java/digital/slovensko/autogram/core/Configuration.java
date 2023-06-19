package digital.slovensko.autogram.core;

import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(Configuration.class.getResourceAsStream("configuration.properties"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load ConfigurationProperties", e);
        }
    }

    public static String getProperty(String key) {
        if (!PROPERTIES.containsKey(key))
            throw new IllegalArgumentException("Requested unknown property key: " + key);

        return PROPERTIES.getProperty(key);
    }
}