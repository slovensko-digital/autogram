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

    private InfoEndpoint infoEndpoint = new InfoEndpoint();

    private SignEndpoint signEndpoint = new SignEndpoint();

    private DocumentationEndpoint documentationEndpoint = new DocumentationEndpoint();

    public Server(String hostname, int port, boolean devMode) {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (Exception e) {
            throw new RuntimeException("Could not start server", e);
        }

        server.createContext("/", infoEndpoint);
        server.createContext("/sign", signEndpoint);
        if (devMode) {
            server.createContext("/documentation", documentationEndpoint);
        }

        // Run requests in separate threads
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        // TODO: Add exiting based on the requested timeout
        System.out.println("Running in server mode on " + server.getAddress().toString());
        if (devMode) {
            var docsAddress = "http:/" + server.getAddress().toString() + "/documentation";
            System.out.println("Documentation is available in dev mode at " + docsAddress);
        }
    }

    public void setInfo(Info info) {
        infoEndpoint.setInfo(info);
    }

    public void setOnSign(Function<Document, CompletableFuture<Document>> onSign) {
        signEndpoint.setOnSign(onSign);
    }

}
