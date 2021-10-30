package com.octosign.whitelabel.communication.server;

import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.endpoint.DocumentationEndpoint;
import com.octosign.whitelabel.communication.server.endpoint.InfoEndpoint;
import com.octosign.whitelabel.communication.server.endpoint.SignEndpoint;
import com.octosign.whitelabel.error_handling.SignerException;
import com.octosign.whitelabel.error_handling.UserException;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Server {

    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final int DEFAULT_PORT = 37200;

    private final InfoEndpoint infoEndpoint;
    private final SignEndpoint signEndpoint;
    private final DocumentationEndpoint documentationEndpoint;

    private final HttpServer server;

    // Local development mode
    private boolean devMode;

    // HTTP allowed origin
    private String allowedOrigin = "*";

    // HMAC hex secret key
    private String secretKey;

    public Server(int initialNonce) throws SignerException {
        this(DEFAULT_HOSTNAME, DEFAULT_PORT, initialNonce);
    }

    public Server(String hostname, int port, int initialNonce) throws UserException {
        try {
            server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (IOException e) {
            throw new UserException("error.serverNotCreated", e);
        }

        documentationEndpoint = new DocumentationEndpoint(this);
        infoEndpoint = new InfoEndpoint(this);
        signEndpoint = new SignEndpoint(this, initialNonce);
    }

    public void start() {
        server.createContext("/", infoEndpoint);
        server.createContext("/sign", signEndpoint);

        if (devMode)
            server.createContext("/documentation", documentationEndpoint);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public void setOnSign(Function<SignatureUnit, Future<Document>> onSign) {
        signEndpoint.setOnSign(onSign);
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public void setAllowedOrigin(String allowedOrigin) { this.allowedOrigin = allowedOrigin; }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setInfo(Info info) { infoEndpoint.setInfo(info); }


    public boolean isDevMode() {
        return devMode;
    }

    public String getAllowedOrigin() { return allowedOrigin; }

    public String getSecretKey() {
        return secretKey;
    }

    public InetSocketAddress getAddress() { return server.getAddress(); }

    public String getDefaultHostname() { return DEFAULT_HOSTNAME; }
}
