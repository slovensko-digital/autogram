package com.octosign.whitelabel.communication.server;

import java.io.FileInputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.security.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.SignedData;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.server.endpoint.DocumentationEndpoint;
import com.octosign.whitelabel.communication.server.endpoint.InfoEndpoint;
import com.octosign.whitelabel.communication.server.endpoint.SignEndpoint;
import com.octosign.whitelabel.error_handling.UserException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;

import static com.octosign.whitelabel.ui.utils.ConfigurationProperties.*;
import static com.octosign.whitelabel.ui.utils.I18n.translate;

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

    private boolean isHttps;

    public Server(String hostname, int port, int initialNonce, boolean isHttps) {
        this.isHttps = isHttps;

        server = getServer(hostname, port, initialNonce, isHttps);
        documentationEndpoint = new DocumentationEndpoint(this);
        infoEndpoint = new InfoEndpoint(this);
        signEndpoint = new SignEndpoint(this, initialNonce);
    }

    public void start() {
        server.createContext("/", infoEndpoint);
        server.createContext("/sign", signEndpoint);

//        TODO decide this along with TODO in ../Main.java
//        if (devMode) {
            server.createContext("/documentation", documentationEndpoint);
//        }

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

    public void setOnSign(Function<SignatureUnit, Future<SignedData>> onSign) {
        signEndpoint.setOnSign(onSign);
    }

    public boolean isHttps() {
        return this.isHttps;
    }

    public InetSocketAddress getAddress() {
        return server.getAddress();
    }

    private HttpServer getServer(String hostname, int port, int initialNonce, boolean isHttps) {
        HttpServer server;
        try {
            if (isHttps) {
                server = HttpsServer.create(new InetSocketAddress(hostname, port), 0);

                var p12file = Paths.get(System.getProperty("user.home"), getProperty("file.ssl.pkcs12.cert")).toFile();
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                char[] password = "".toCharArray();

                KeyStore ks = KeyStore.getInstance("PKCS12");
                FileInputStream fis = new FileInputStream(p12file);
                ks.load(fis, password);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                ((HttpsServer)server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    public void configure(HttpsParameters params) {
                        try {
                            SSLContext c = SSLContext.getDefault();
                            SSLEngine engine = c.createSSLEngine();
                            params.setNeedClientAuth(false);
                            params.setCipherSuites(engine.getEnabledCipherSuites());
                            params.setProtocols(engine.getEnabledProtocols());
                            SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                            params.setSSLParameters(defaultSSLParameters);
                        } catch (Exception e) {
                            throw new UserException("error.serverNotCreated", e);
                        }
                    }
                });
            } else {
                server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
            }
        } catch (BindException e) {
            throw new UserException("error.launchFailed.header", translate("error.launchFailed.addressInUse.description", port), e);
        } catch (Exception e) {
            throw new UserException("error.serverNotCreated", e);
        }

        return server;
    }
}
