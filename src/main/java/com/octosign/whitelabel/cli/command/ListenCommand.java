package com.octosign.whitelabel.cli.command;

import javafx.application.Application.Parameters;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.octosign.whitelabel.cli.command.ListenCommand.Validations.*;
import static com.octosign.whitelabel.ui.Utils.isPresent;

/**
 * Launch application in the server mode - listening
 */
public class ListenCommand extends Command {

    public static final String NAME = "listen";

    private String scheme;

    private String protocol;

    private String host;

    private int port;

    private String origin;

    private String secretKey;

    private int initialNonce;

    /**
     * Parse command using a URL
     *
     * EXAMPLES
     * signer://listen?protocol=https&host=localhost&port=37200&origin=*&key=abc123qwer456xyz&nonce=16 (currently active)
     *
     * Used when the application is launched using the custom protocol
     */
    ListenCommand(URI url) {
        super(url);
        this.scheme = validateScheme(url.getScheme());
        var query = QueryParams.parseQueryString(url.getQuery());

        initialize(query.get("protocol"), query.get("host"), query.get("port"),
                query.get("origin"), query.get("key"), query.get("nonce"));
    }

    /**
     * Parse command using JavaFX Application Parameters
     *
     * Used when the application is not launched using the custom protocol
     */
    ListenCommand(Parameters parameters) {
        super(parameters);
        var named = parameters.getNamed();

        initialize(named.get("protocol"), named.get("host"), named.get("port"),
                named.get("origin"), named.get("key"), named.get("nonce"));
    }

    private void initialize(String protocol, String host, String port, String origin, String key, String nonce) {
        if (isPresent(protocol))
            this.protocol = validateProtocol(protocol);

        if (isPresent(host))
            this.host = validateHost(host);

        if (isPresent(port))
            this.port = validatePort(port);

        if (isPresent(origin))
            this.origin = validateOrigin(origin);

        if (isPresent(key))
            this.secretKey = validateSecretKey(key);

        if (isPresent(nonce))
            this.initialNonce = validateInitialNonce(nonce);
    }

    public boolean isRequiredSSL() {
        return protocol.equalsIgnoreCase("https");
    }

    public String getScheme() {
        return scheme;
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


    public static class Validations {
        private static final int MAX_PORT_NUMBER = 65535;
        private static final String VALID_ORIGIN_REGEX = "^\\*|((https?:\\/\\/)([^\\s.:/\\\\]+[\\.])*([^\\s.:/\\\\]+)(:\\d+)?)$";
        private static final String VALID_HOSTNAME_REGEX = "localhost|([1-9]{1}[0-9]{0,2}.[1-9]{1}[0-9]{0,2}.[1-9]{1}[0-9]{0,2}.[1-9]{1}[0-9]{0,2})|(\\w*.\\w*.\\w*)";

        public static String validateScheme(String scheme) {
            if (scheme.equalsIgnoreCase("signer"))
                return scheme.toLowerCase(Locale.ROOT);

            throw new IllegalArgumentException("Invalid scheme: " + scheme);
        }

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

        private static int validInteger(String input) {
            try {
                return Integer.parseInt(input);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Input [" + input + "] is not a valid integer value. Details: " + e.getMessage());
            }
        }
    }
}
