package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.server.Server;
import com.sun.net.httpserver.HttpExchange;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class DocumentationEndpoint extends Endpoint {

    private final String docsPath = "./docs/static/api";

    private final String htmlName = "index.html";

    private final String yamlName = "server.yml";

    public DocumentationEndpoint(Server server) {
        super(server);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String cwd = System.getProperty("user.dir");
        boolean isYaml = exchange.getRequestURI().getPath().endsWith(".yml");

        var file = Paths.get(cwd, docsPath, isYaml ? yamlName : htmlName);
        var mimeType = isYaml ? "text/yaml" : "text/html";

        var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, 0);

        // automatically closes both streams
        try (var fileStream = new FileInputStream(file.toString()); var responseStream = exchange.getResponseBody()) {
            fileStream.transferTo(responseStream);
        }
    }

    @Override
    protected String[] getAllowedMethods() {
        return new String[]{ "GET" };
    }

}
