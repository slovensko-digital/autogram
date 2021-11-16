package com.octosign.whitelabel.communication.server;

import java.net.BindException;
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
import com.octosign.whitelabel.error_handling.UserException;
import com.sun.net.httpserver.HttpServer;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;
import static com.octosign.whitelabel.ui.I18n.translate;

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

    public Server(int initialNonce) {
        this(
            getProperty("server.defaultAddress"),
            Integer.parseInt(getProperty("server.defaultPort")),
            initialNonce
        );
    }

    public Server(String hostname, int port, int initialNonce) {
        try {
            server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (BindException e) {
            throw new UserException("error.launchFailed.header", translate("error.launchFailed.addressInUse.description", port), e);
        } catch (Exception e) {
            throw new UserException("error.serverNotCreated", e);
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

    public void stop() {
        server.stop(0);
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
