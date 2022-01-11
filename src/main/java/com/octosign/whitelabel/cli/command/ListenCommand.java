package com.octosign.whitelabel.cli.command;

import java.util.*;
import java.util.regex.Pattern;

import static com.octosign.whitelabel.cli.command.ListenCommand.Validations.*;
import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;
import static com.octosign.whitelabel.ui.Utils.isPresent;
import static java.util.Optional.ofNullable;

/**
 * Launch application in the server mode - listening
 */
public class ListenCommand extends Command {
    public static final String NAME = "listen";

    private String protocol;

    private String host;

    private int port;

    private String origin;

    private String secretKey;

    private int initialNonce;

    private String language;

    /**
     * Parse command using JavaFX Application Parameters
     *
     * Used when the application is not launched using the custom protocol
     */
    ListenCommand(Map<String, String> params) {
        super(params);

        var protocol = ofNullable(params.get("protocol")).orElse(getProperty("server.defaultProtocol"));
        var host = ofNullable(params.get("host")).orElse(getProperty("server.defaultAddress"));
        var port = ofNullable(params.get("port")).orElse(getProperty("server.defaultPort"));
        var origin = ofNullable(params.get("origin")).orElse(getProperty("server.defaultOrigin"));
        var language = ofNullable(params.get("language")).orElse(Locale.getDefault().getLanguage());
        var key = params.get("key");
        var nonce = params.get("nonce");

        this.protocol = validateProtocol(protocol);
        this.host = validateHost(host);
        this.port = validatePort(port);
        this.origin = validateOrigin(origin);
        this.language = validateLanguage(language);

        if (isPresent(key))
            this.secretKey = validateSecretKey(key);
        if (isPresent(nonce))
            this.initialNonce = validateInitialNonce(nonce);
    }

    public boolean isRequiredSSL() {
        return protocol.equalsIgnoreCase("https");
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getOrigin() {
        return origin;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public int getInitialNonce() {
        return initialNonce;
    }

    public String getLanguage() {
        return language;
    }

    public static class Validations {
        private static final int MAX_PORT_NUMBER = 65535;
        private static final String VALID_ORIGIN_REGEX = "^\\*|((https?:\\/\\/)([^\\s.:/\\\\]+[\\.])*([^\\s.:/\\\\]+)(:\\d+)?)$";
        private static final String VALID_HOSTNAME_REGEX = "^([a-z]|[A-Z]|[0-9]|[\u00a0-\uffff]|-)+$";
        private static final String VALID_LANGUAGE_REGEX = "^([a-z]|[A-Z]|[0-9]|_|-)+$";

        public static String validateProtocol(String protocol) {
            if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https"))
                return protocol.toLowerCase(Locale.ROOT);

            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }

        public static String validateHost(String host) {
            if (Pattern.compile(VALID_HOSTNAME_REGEX).matcher(host).matches())
                return host;
            else
                throw new IllegalArgumentException("Invalid hostname " + host);
        }

        public static int validatePort(String input) {
            int port = validInteger(input);

            if (port <= MAX_PORT_NUMBER && port >= 1)
                return port;
            else
                throw new IllegalArgumentException("Port " + input + " is outside the allowed range (1-65535)");
        }

        public static String validateOrigin(String origin) {
            if (Pattern.compile(VALID_ORIGIN_REGEX).matcher(origin).matches())
                return origin;
            else
                throw new IllegalArgumentException("Origin " + origin + " is invalid.");
        }

        public static String validateSecretKey(String key) {
            if (key.length() < 16) {
                throw new IllegalArgumentException("Secret key is too short. Minimum length is 16 characters.");
            }

            return key;
        }

        public static int validateInitialNonce(String nonce) {
            return validInteger(nonce);
        }

        public static String validateLanguage(String language) {
            if (Pattern.compile(VALID_LANGUAGE_REGEX).matcher(language).matches()) {
                return language.toLowerCase().strip()
                        .replaceAll("[_/,.;+\\\\]", "-")
                        .replaceFirst("en-[a-z]{2}", "en-us");

            } else
                throw new IllegalArgumentException("Language " + language + " is not valid.");
        }

        private static int validInteger(String input) {
            try {
                return Integer.parseInt(input);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Input [" + input + "] is not a valid integer value. Details: " + e.getMessage());
            }
        }
    }
}
