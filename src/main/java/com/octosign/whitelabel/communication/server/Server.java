package com.octosign.whitelabel.communication.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.endpoint.DocumentationEndpoint;
import com.octosign.whitelabel.communication.server.endpoint.InfoEndpoint;
import com.octosign.whitelabel.communication.server.endpoint.SignEndpoint;
import com.sun.net.httpserver.HttpServer;

public class Server {

    private final InfoEndpoint infoEndpoint;

    private final SignEndpoint signEndpoint;

    private final DocumentationEndpoint documentationEndpoint;

    private final HttpServer server;

    /**
     * Local development mode
     */
    private boolean devMode;

    /**
     * HTTP allowed origin
     */
    private String allowedOrigin = "*";

    /**
     * HMAC hex secret key
     */
    private String secretKey;

    public Server(String hostname, int port, int initialNonce) {
        try {
            server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (Exception e) {
            throw new RuntimeException("Could not create server", e);
        }

        documentationEndpoint = new DocumentationEndpoint(this);
        infoEndpoint = new InfoEndpoint(this);
        signEndpoint = new SignEndpoint(this, initialNonce);
    }

    public void start() {
        server.createContext("/", infoEndpoint);
        server.createContext("/sign", signEndpoint);
        if (devMode) {
            server.createContext("/documentation", documentationEndpoint);
        }

        // Run requests in separate threads
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setInfo(Info info) {
        infoEndpoint.setInfo(info);
    }

    public void setOnSign(Function<SignatureUnit, Future<Document>> onSign) {
        signEndpoint.setOnSign(onSign);
    }

    public InetSocketAddress getAddress() {
        return server.getAddress();
    }

}
