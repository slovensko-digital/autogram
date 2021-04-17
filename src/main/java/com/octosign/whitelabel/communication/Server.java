package com.octosign.whitelabel.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.octosign.whitelabel.communication.ErrorResponse.Code;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
    private static ServerInfo info;
    private static Function<Document, CompletableFuture<Document>> onSign;

    private static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, 405, "Allowed methods: GET.");
                return;
            }

            sendResponse(exchange, 200, info);
        }
    }

    private static class SignHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, 405, "Allowed methods: POST.");
                return;
            }

            if (onSign == null) {
                sendResponse(
                    exchange,
                    409,
                    new ErrorResponse(Code.NOT_READY, "Server is not yet ready for signing.")
                );
                return;
            }

            var inputDocument = getRequestBody(exchange, Document.class);
            if (inputDocument == null) return;

            try {
                var signedDocument = onSign.apply(inputDocument).get();
                sendResponse(exchange, 200, signedDocument);
                return;
            } catch (Exception e) {
                // TODO: We should do a better job with the error response here:
                // We can differentiate between application errors (500), user errors (502), missing certificate/UI closed (503)
                sendResponse(exchange, 500, new ErrorResponse(Code.SIGNING_FAILED, "Signing failed.", e.getMessage()));
                return;
            }
        }
    }

    public Server(String hostname, int port) {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (Exception e) {
            throw new RuntimeException("Could not start server", e);
        }

        server.createContext("/", new InfoHandler());
        server.createContext("/sign", new SignHandler());

        // Run requests in separate threads
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void setInfo(ServerInfo info) {
        Server.info = info;
    }

    public void setOnSign(Function<Document, CompletableFuture<Document>> onSign) {
        Server.onSign = onSign;
    }

    /**
     * Parses request body respecting the Content-Type
     * @throws IOException
     * @throws JsonSyntaxException
     */
    private static <E> E getRequestBody(HttpExchange exchange, Class<E> requestClass) throws IOException {
        var contentType = exchange.getRequestHeaders().get("Content-Type");
        var body = exchange.getRequestBody();

        if (contentType.contains("*/*") || contentType.contains("application/json")) {
            try {
                return new Gson().fromJson(new String(body.readAllBytes(), StandardCharsets.UTF_8), requestClass);
            } catch (JsonSyntaxException e) {
                sendResponse(exchange, 400, new ErrorResponse(Code.MALFORMED_INPUT, "Request body is invalid."));
            }
        } else {
            sendResponse(exchange, 415, new ErrorResponse(Code.UNSUPPORTED_FORMAT, "Supported MIME types: application/json."));
        }

        return null;
    }

    /**
     * Sends response respecting the Accept header
     *
     * @param exchange
     * @param status
     * @param responseObject
     * @throws IOException
     */
    private static void sendResponse(HttpExchange exchange, int status, Object responseObject) throws IOException {
        var accept = exchange.getRequestHeaders().get("Accept");
        var body = exchange.getResponseBody();
        var headers = exchange.getResponseHeaders();

        if (accept.contains("*/*") || accept.contains("application/json")) {
            var response = new Gson().toJson(responseObject).getBytes(StandardCharsets.UTF_8);
            headers.set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, response.length);
            body.write(response);
            body.close();
        } else {
            var response = "Supported MIME types: application/json.";
            headers.set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(406, response.length());
            body.write(response.getBytes());
            body.close();
        }
    }
}
