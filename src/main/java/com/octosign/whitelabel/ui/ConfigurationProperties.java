package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;


public class ConfigurationProperties {
    private static final Properties PROPERTIES;

    public static String getProperty(String key) {
        if (!PROPERTIES.containsKey(key))
            throw new IntegrationException(Code.UNEXPECTED_ERROR, "Requested unknown property key: " + key);

        return PROPERTIES.getProperty(key);
    }

    public static String[] getPropertyArray(String key) {
        assert(key.startsWith("[]"));

        return Arrays.stream(getProperty(key).split("\\w*<@>\\w*"))
            .map(String::strip)
            .toArray(String[]::new);
    }

    static {
        PROPERTIES = new Properties();

        try {
            PROPERTIES.load(ConfigurationProperties.class.getResourceAsStream("configuration.properties"));
        }
        catch (IOException e) {
            throw new IntegrationException(Code.UNEXPECTED_ERROR, e);
        }
    }
}
