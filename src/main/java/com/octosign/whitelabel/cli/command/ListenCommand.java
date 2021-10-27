package com.octosign.whitelabel.cli.command;

import javafx.application.Application.Parameters;

import java.net.URI;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.octosign.whitelabel.cli.command.ListenCommand.Validations.*;

/**
 * Launch application in the server mode - listening
 */
public class ListenCommand extends Command {

    public static final String NAME = "listen";

    private int port;

    private String origin;

    private String secretKey;

    private int initialNonce;

    /**
     * Parse command using a URL
     *
     * Used when the application is launched using the custom protocol
     */
    ListenCommand(URI url) {
        super(url);
        var params = Arrays.stream(url.getPath().split("/"))
                                    .map(String::strip)
                                    .map(this::nullifyBlank)
                                    .toArray(String[]::new);

        if (params.length > 1)
            setPort(params[1]);

        if (params.length > 2)
            setOrigin(params[2]);

        if (params.length > 3)
            setSecretKey(params[3]);

        if (params.length > 4)
            setInitialNonce(params[4]);

    }

    /**
     * Parse command using JavaFX Application Parameters
     *
     * Used when the application is not launched using the custom protocol
     */
        ListenCommand(Parameters parameters) {
        super(parameters);
        var named = parameters.getNamed();

        if (named.containsKey("port"))
            setPort(named.get("port"));

        if (named.containsKey("origin"))
            setOrigin(named.get("origin"));

        if (named.containsKey("key"))
            setSecretKey(named.get("key"));

        if (named.containsKey("nonce"))
            setInitialNonce(named.get("nonce"));
    }

    private String nullifyBlank(String value) { return value != null && value.isBlank() ? null : value; }

    public int getPort() { return port; }
    public String getOrigin() { return origin; }
    public String getSecretKey() { return secretKey; }
    public int getInitialNonce() { return initialNonce; }

    private void setPort(String port) { this.port = validPort(port); }
    private void setOrigin(String origin) { this.origin = validOrigin(origin); }
    private void setSecretKey(String secretKey) { this.secretKey = validSecretKey(secretKey); }
    private void setInitialNonce(String initialNonce) { this.initialNonce = validInitialNonce(initialNonce); }

    public static class Validations {
        private static final int MAX_PORT_NUMBER = 65535;
        private static final String VALID_ORIGIN_REGEX = "^(\\*)|(((http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?)|((\\d{1,3}\\.){3}\\d{1,3}))$";

        public static int validPort(String port) {
            var result = validInteger(port);
            if (result <= MAX_PORT_NUMBER)
                return result;
            else
                throw new IllegalArgumentException(String.format("Port number %s is outside allowed range (1-65535)", result));
        }

        public static String validOrigin(String origin) {
            if (Pattern.compile(VALID_ORIGIN_REGEX).matcher(origin).matches())
                return origin;
            else
                throw new IllegalArgumentException(String.format("Origin %s is invalid.", origin));
        }

        // TODO implement this
        public static String validSecretKey(String key) {
            return key;
        }

        // TODO implement this
        public static int validInitialNonce(String nonce) {
            return validInteger(nonce);
        }

        private static int validInteger(String input) {
            try {
                return Integer.parseInt(input);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Input '%s' is not a valid integer value. Detail: %s", input, e.getMessage()));
            }
        }
    }
}
