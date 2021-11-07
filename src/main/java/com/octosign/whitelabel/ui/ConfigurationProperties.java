package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.io.IOException;
import java.util.Properties;

public class ConfigurationProperties {
    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(ConfigurationProperties.class.getResourceAsStream("configuration.properties"));
        }
        catch (IOException e) {
            throw new IntegrationException(Code.PROPERTIES_NOT_LOADED, e);
        }
    }

    public static String getProperty(String key) {
        if (!PROPERTIES.containsKey(key))
            throw new IntegrationException(Code.PROPERTY_NOT_FOUND, "Requested unknown property key: " + key);

        return PROPERTIES.getProperty(key);
    }
}
