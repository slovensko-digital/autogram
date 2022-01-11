package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.ui.Main;
import com.sun.net.httpserver.HttpExchange;

import static java.util.Objects.requireNonNull;

public class DocumentationEndpoint extends Endpoint {

    public DocumentationEndpoint(Server server) {
        super(server);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) {
        boolean isYaml = exchange.getRequestURI().getPath().endsWith(".yml");
        var mimeType = isYaml ? "text/yaml" : "text/html";
        var filename = isYaml ? "server.yml" : "index.html";

        exchange.getResponseHeaders().set("Content-Type", mimeType);

        // automatically closes both streams
        try (var stream =  Main.class.getResourceAsStream(filename);
             var responseStream = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(200, 0);

            requireNonNull(stream).transferTo(responseStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String[] getAllowedMethods() {
        return new String[]{ "GET" };
    }
}
