package digital.slovensko.autogram.util;

import java.io.IOException;
import java.util.Properties;

public class ConfigurationProperties {
    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(ConfigurationProperties.class.getResourceAsStream("configuration.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load ConfigurationProperties", e);
        }
    }

    public static String getProperty(String key) {
        if (!PROPERTIES.containsKey(key))
            throw new RuntimeException("Requested unknown property key: " + key);

        return PROPERTIES.getProperty(key);
    }
}