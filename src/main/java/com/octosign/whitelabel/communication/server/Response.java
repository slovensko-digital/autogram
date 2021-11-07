package com.octosign.whitelabel.communication.server;

import com.google.common.collect.ImmutableMap;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;

/**
 * Server response that conforms to request headers
 */
public class Response<U> {

    private final HttpExchange exchange;

    private final Map<MimeType, BodyFormat> BODY_FORMATS = ImmutableMap.of(
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

            // TODO very similar method in Request - lets merge them
            bodyFormat = BODY_FORMATS.keySet().stream()
                .filter(m -> m.equalsTypeSubtype(contentMimeType))
                .findFirst()
                .map(BODY_FORMATS::get)
                .orElse(BODY_FORMATS.get(MimeType.ANY));
        } else {
            bodyFormat = BODY_FORMATS.get(MimeType.ANY);
        }
    }

    // TODO modify this to fit the above, then extract it and make equal with the almost identical method in Request class
    public <U extends Map<String, List<String>>> BodyFormat extractBodyFormat(U source) {
        var contentType = source.get("Accept");
        var defaultBodyFormat = BODY_FORMATS.get(MimeType.ANY);
        if (contentType == null)
            return defaultBodyFormat;

        var contentMimeType = MimeType.parse(contentType.get(0));

        return BODY_FORMATS.keySet().stream()
                .filter(m -> m.equalsTypeSubtype(contentMimeType))
                .findFirst()
                .map(BODY_FORMATS::get)
                .orElse(defaultBodyFormat);
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

    public void send() throws IOException {
        var headers = exchange.getResponseHeaders();
        var body = bodyFormat.to(getBody());

        // TODO: Check Accept header instead of hardcoding UTF-8
        var bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        headers.set("Content-Type", bodyFormat.getMimeType().toString());
        exchange.sendResponseHeaders(statusCode, bodyBytes.length);

        // automatically closes stream
        try (var stream = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(statusCode, bodyBytes.length);
            stream.write(bodyBytes);
        }
    }
}
