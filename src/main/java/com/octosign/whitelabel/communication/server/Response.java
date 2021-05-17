package com.octosign.whitelabel.communication.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.octosign.whitelabel.communication.server.format.JsonFormat;
import com.sun.net.httpserver.HttpExchange;

/**
 * Server response that conforms to request headers
 */
public class Response<U> {

    private final HttpExchange exchange;

    private final Map<String, BodyFormat> bodyFormats;

    private final BodyFormat bodyFormat;

    private int statusCode = HttpURLConnection.HTTP_OK;

    private U body;

    public Response(HttpExchange exchange) {
        this.exchange = exchange;

        bodyFormats = Map.of(
            JsonFormat.MIME_TYPE, new JsonFormat(),
            "*/*", new JsonFormat() // Default format
        );

        var contentType = exchange.getRequestHeaders().get("Accept");
        bodyFormat = contentType.stream()
            .map((mimeType) -> mimeType.split(";")[0].toLowerCase())
            .filter((String mimeType) -> bodyFormats.containsKey(mimeType))
            .findFirst()
            .map((mimeType) -> bodyFormats.get(mimeType))
            // We would rather return something in unaccepted type than nothing
            .orElseGet(() -> bodyFormats.get("*/*"));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Response<U> setStatusCode(int statusCode) {
        statusCode = statusCode;
        return this;
    }

    public U getBody() {
        return body;
    }

    public Response<U> setBody(U body) {
        body = body;
        return this;
    }

    public Response<U> asError(int httpCode, U error) {
        setStatusCode(httpCode);
        setBody(error);
        return this;
    }

    public void send() throws IOException {
        var stream = exchange.getResponseBody();
        var headers = exchange.getResponseHeaders();

        var body = bodyFormat.to(getBody());
        // TODO: Check Accept header instead of hardcoding UTF-8
        var bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        headers.set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bodyBytes.length);
        stream.write(bodyBytes);
        stream.close();
    }

}
