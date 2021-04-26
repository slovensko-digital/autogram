package com.octosign.whitelabel.communication.server.endpoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

public class DocumentationEndpoint extends Endpoint {

    private final String docsPath = "./docs";

    private final String htmlName = "server-api.html";

    private final String yamlName = "server-api.yml";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String cwd = System.getProperty("user.dir");
        boolean isYaml = exchange.getRequestURI().getPath().endsWith(".yml");

        var file = Paths.get(cwd, docsPath, isYaml ? yamlName : htmlName);
        var mimeType = isYaml ? "text/yaml" : "text/html";

        var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, 0);

        var fileStream = new FileInputStream(file.toString());
        var responseStream = exchange.getResponseBody();
        fileStream.transferTo(responseStream);
        fileStream.close();
        responseStream.close();
    }

    @Override
    protected String[] getAllowedMethods() {
        return new String[]{ "GET" };
    }

}
