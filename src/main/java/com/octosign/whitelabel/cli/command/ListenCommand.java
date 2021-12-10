package com.octosign.whitelabel.cli.command;

import javafx.application.Application.Parameters;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static com.octosign.whitelabel.cli.command.ListenCommand.Validations.*;
import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;

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

    private Locale language = Locale.getDefault();

    /**
     * Parse command using a URL
     *
     * EXAMPLE:
     * signer://listen?protocol=https&host=localhost&port=37200&origin=*&key=abc12&nonce=16&language=sk (currently active)
     *
     * Used when the application is launched using the custom protocol
     */
    ListenCommand(URI url) {
        super(url);
        var parameters = QueryParams.parseQueryString(url.getQuery());

        initialize(parameters.asMap());
    }

    /**
     * Parse command using JavaFX Application Parameters
     *
     * Used when the application is not launched using the custom protocol
     */
    ListenCommand(Parameters parameters) {
        super(parameters);
        initialize(parameters.getNamed());
    }

    private void initialize(Map<String, String> params) {
        var protocol = params.getOrDefault("protocol", getProperty("server.defaultProtocol"));
        this.protocol = validateProtocol(protocol);

        var host = params.getOrDefault("host", getProperty("server.defaultAddress"));
        this.host = validateHost(host);

        var port = params.getOrDefault("port", getProperty("server.defaultPort"));
        this.port = validatePort(port);

        var origin = params.getOrDefault("origin", getProperty("server.defaultOrigin"));
        this.origin = validateOrigin(origin);

        if (params.containsKey("key"))
            this.secretKey = validateSecretKey(params.get("key"));

        if (params.containsKey("nonce"))
            this.initialNonce = validateInitialNonce(params.get("nonce"));

        if (params.containsKey("language"))
            this.language = validateLanguage(params.get("language"));
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

    public Locale getLanguage() {
        return language;
    }

    public static class Validations {
        private static final int MAX_PORT_NUMBER = 65535;
        private static final String VALID_ORIGIN_REGEX = "^\\*|((https?:\\/\\/)([^\\s.:/\\\\]+[\\.])*([^\\s.:/\\\\]+)(:\\d+)?)$";
        private static final String VALID_HOSTNAME_REGEX = "localhost|([1-9]{1}[0-9]{0,2}.[1-9]{1}[0-9]{0,2}.[1-9]{1}[0-9]{0,2}.[1-9]{1}[0-9]{0,2})|(\\w*.\\w*.\\w*)";
        private static final String VALID_LANGUAGE_REGEX = "^[A-Za-z]{2,4}([_-][A-Za-z]{4})?([_-]([A-Za-z]{2}|[0-9]{3}))?$";

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

        // TODO implement these two
        public static String validateSecretKey(String key) {
            return key;
        }

        public static int validateInitialNonce(String nonce) {
            return validInteger(nonce);
        }

        public static Locale validateLanguage(String language) {
            if (Pattern.compile(VALID_LANGUAGE_REGEX).matcher(language).matches())
                return Locale.forLanguageTag(language);
            else
                throw new IllegalArgumentException("Language " + language + " is not a valid Locale.");
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
