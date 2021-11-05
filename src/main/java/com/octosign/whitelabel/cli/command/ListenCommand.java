package com.octosign.whitelabel.cli.command;

import javafx.application.Application.Parameters;

import java.net.URI;

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

        var params = url.getPath().split("/");

        if (params.length > 1 && !params[1].isBlank()) {
            setPort(params[1]);
        }

        if (params.length > 2 && !params[2].isBlank()) {
            setOrigin(params[2]);
        }

        if (params.length > 3 && !params[3].isBlank()) {
            setSecretKey(params[3]);
        }

        if (params.length > 4 && !params[4].isBlank()) {
            setInitialNonce(params[4]);
        }
    }

    /**
     * Parse command using JavaFX Application Parameters
     *
     * Used when the application is not launched using the custom protocol
     */
    ListenCommand(Parameters parameters) {
        super(parameters);

        var named = parameters.getNamed();

        if (named.containsKey("port")) {
            setPort(named.get("port"));
        }

        if (named.containsKey("origin")) {
            setOrigin(named.get("origin"));
        }

        if (named.containsKey("key")) {
            setSecretKey(named.get("key"));
        }

        if (named.containsKey("nonce")) {
            setInitialNonce(named.get("nonce"));
        }
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

    private void setPort(String port) {
        // TODO: Add validation
        this.port = Integer.parseInt(port);
    }

    private void setOrigin(String origin) {
        // TODO: Add validation
        this.origin = origin;
    }

    private void setSecretKey(String secretKey) {
        // TODO: Add validation
        this.secretKey = secretKey;
    }

    private void setInitialNonce(String initialNonce) {
        // TODO: Add validation
        this.initialNonce = Integer.parseInt(initialNonce);
    }

}
