package com.octosign.whitelabel.communication.server;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;

import com.octosign.whitelabel.communication.Info;
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

    // Local development mode
    private boolean devMode;

    // HTTP allowed origin
    private String allowedOrigin = "*";

    // HMAC hex secret key
    private String secretKey;

    public Server(String hostname, int port, int initialNonce) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (Exception e) {
            throw new RuntimeException("Could not create server", e);
        }

        this.documentationEndpoint = new DocumentationEndpoint(this);
        this.infoEndpoint = new InfoEndpoint(this);
        this.signEndpoint = new SignEndpoint(this, initialNonce);
    }

    public void start() {
        this.server.createContext("/", this.infoEndpoint);
        this.server.createContext("/sign", this.signEndpoint);

        if (this.devMode) {
            this.server.createContext("/documentation", this.documentationEndpoint);
        }

        // Run requests in separate threads
        this.server.setExecutor(Executors.newCachedThreadPool());
        this.server.start();
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public boolean isDevMode() {
        return this.devMode;
    }

    public String getAllowedOrigin() {
        return this.allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setInfo(Info info) {
        this.infoEndpoint.setInfo(info);
    }

    public void setOnSign(Function<Document, CompletableFuture<Document>> onSign) {
        this.signEndpoint.setOnSign(onSign);
    }

    public InetSocketAddress getAddress() {
        return this.server.getAddress();
    }

}
