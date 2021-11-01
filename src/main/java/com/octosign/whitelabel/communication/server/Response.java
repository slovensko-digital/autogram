package com.octosign.whitelabel.communication.server;

import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.sun.net.httpserver.HttpExchange;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;
import static com.octosign.whitelabel.ui.I18n.translate;

/**
 * Server response that conforms to request headers
 */
public class Response<U> {

    private final HttpExchange exchange;

    private final Map<MimeType, BodyFormat> bodyFormats = Map.of(
        MimeType.JSON_UTF8, JSON,
        MimeType.ANY, JSON // Default format
    );

    private final BodyFormat bodyFormat;

    private int statusCode = HttpURLConnection.HTTP_OK;

    private U body;

    public Response(HttpExchange exchange) {
        this.exchange = exchange;

        var accept = exchange.getRequestHeaders().get("Accept");

        if (accept != null) {
            // TODO: Consider all mime types
            var contentMimeType = Arrays.stream(accept.get(0).split(","))
                .map(MimeType::parse)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

            bodyFormat = bodyFormats.keySet().stream()
                .filter(m -> m.equalsTypeSubtype(contentMimeType))
                .findFirst()
                .map(bodyFormats::get)
                .orElse(bodyFormats.get(MimeType.ANY));
        } else {
            bodyFormat = bodyFormats.get(MimeType.ANY);
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Response<U> setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public U getBody() {
        return body;
    }

    public Response<U> setBody(U body) {
        this.body = body;
        return this;
    }

    public Response<U> asError(int httpCode, U error) {
        setStatusCode(httpCode);
        setBody(error);
        return this;
    }

    public void send() throws IOException {
        var headers = exchange.getResponseHeaders();
        var body = bodyFormat.to(getBody());

        // TODO: Check Accept header instead of hardcoding UTF-8
        var bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        headers.set("Content-Type", bodyFormat.getMimeType().toString());

        try (var stream = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(statusCode, bodyBytes.length);
            stream.write(bodyBytes);
        }
    }
}

