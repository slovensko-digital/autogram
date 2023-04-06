package digital.slovensko.autogram.server;


import com.sun.net.httpserver.HttpServer;
import digital.slovensko.autogram.core.Autogram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutogramServer {
    private final Autogram autogram;
    private final HttpServer server;

    public AutogramServer(Autogram autogram) {
        this.autogram = autogram;
        this.server = buildServer();
    }

    public void start() {
        server.createContext("/info", new InfoEndpoint());
        server.createContext("/sign", new SignEndpoint(autogram));
        server.createContext("/docs", new DocumentationEndpoint());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private HttpServer buildServer() {
        HttpServer server;
        try {
            // TODO parameterize from args
            server = HttpServer.create(new InetSocketAddress("localhost", 37200), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    public void stop() {
        ((ExecutorService) server.getExecutor()).shutdown(); // TODO find out why requests hang
        server.stop(1);
    }
}
