package digital.slovensko.autogram.core;

import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static final Properties PROPERTIES;
    private static final Properties BUILD_PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(Configuration.class.getResourceAsStream("configuration.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load ConfigurationProperties", e);
        }

        BUILD_PROPERTIES = new Properties();
        try {
            BUILD_PROPERTIES.load(Configuration.class.getResourceAsStream("/digital/slovensko/autogram/build.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load build.properties", e);
        }
    }

    public static String getProperty(String key) {
        if (!PROPERTIES.containsKey(key))
            throw new RuntimeException("Requested unknown property key: " + key);

        return PROPERTIES.getProperty(key);
    }

    public static String getBuildProperty(String key) {
        if (!BUILD_PROPERTIES.containsKey(key))
            throw new RuntimeException("Requested unknown build property key: " + key);

        return BUILD_PROPERTIES.getProperty(key);
    }
}