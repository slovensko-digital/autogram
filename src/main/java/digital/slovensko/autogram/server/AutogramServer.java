package digital.slovensko.autogram.server;

import static digital.slovensko.autogram.core.Configuration.getProperty;

import java.io.FileInputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.server.filters.AutogramCorsFilter;

public class AutogramServer {
    private final HttpServer server;
    private final Autogram autogram;

    public AutogramServer(Autogram autogram, String hostname, int port, boolean isHttps) {
        this.autogram = autogram;
        this.server = buildServer(hostname, port, isHttps);
    }

    public void start() {
        // Info
        server.createContext("/info", new InfoEndpoint()).getFilters()
                .add(new AutogramCorsFilter("GET"));

        // Documentation
        server.createContext("/docs", new DocumentationEndpoint());

        // Sign
        server.createContext("/sign", new SignEndpoint(autogram)).getFilters()
                .add(new AutogramCorsFilter("POST"));

        // Batch
        server.createContext("/batch", new BatchEndpoint(autogram));

        // Start server
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private HttpServer buildServer(String hostname, int port, boolean isHttps) {
        try {
            if (!isHttps)
                return HttpServer.create(new InetSocketAddress(hostname, port), 0);

            var server = HttpsServer.create(new InetSocketAddress(hostname, port), 0);
            var p12file =
                    Paths.get(System.getProperty("user.home"), getProperty("file.ssl.pkcs12.cert"))
                            .toFile();
            char[] password = "".toCharArray();
            var ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(p12file), password);

            var kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);
            var tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            var sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        var c = SSLContext.getDefault();
                        var engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        var defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception e) {
                        throw new RuntimeException(e); // TODO
                    }
                }
            });

            return server;

        } catch (BindException e) {
            throw new RuntimeException("error.launchFailed.header port is already in use", e); // TODO

        } catch (Exception e) {
            throw new RuntimeException("error.serverNotCreated", e); // TODO
        }
    }

    public void stop() {
        ((ExecutorService) server.getExecutor()).shutdown(); // TODO find out why requests hang
        server.stop(1);
    }
}
