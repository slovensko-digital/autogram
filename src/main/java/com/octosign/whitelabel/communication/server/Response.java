package com.octosign.whitelabel.communication.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.MalformedMimetypeException;
import com.sun.net.httpserver.HttpExchange;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;

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
            var contentMimeType = Arrays.asList(accept.get(0).split(",")).stream()
                .map(raw -> {
                    try {
                        return MimeType.parse(raw);
                    } catch (MalformedMimetypeException e) {
                        throw new IntegrationException(Code.MALFORMED_MIMETYPE, e);
                    }
                })
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

            // TODO very similar method in Request - lets merge them
            bodyFormat = bodyFormats.keySet().stream()
                .filter(m -> m.equalsTypeSubtype(contentMimeType))
                .findFirst()
                .map(bodyFormats::get)
                .orElse(bodyFormats.get(MimeType.ANY));
        } else {
            bodyFormat = bodyFormats.get(MimeType.ANY);
        }
    }

    // TODO modify this to fit the above, then extract it and make equal with the almost identical method in Request class
    public <W extends Map<String, List<String>>> BodyFormat extractBodyFormat(W source) {
        var contentType = source.get("Accept");
        var defaultBodyFormat = bodyFormats.get(MimeType.ANY);
        if (contentType == null)
            return defaultBodyFormat;

        MimeType contentMimeType;
        try {
            contentMimeType = MimeType.parse(contentType.get(0));
        } catch (MalformedMimetypeException e) {
            throw new IntegrationException(Code.MALFORMED_MIMETYPE, e);
        }

        return bodyFormats.keySet().stream()
                .filter(m -> m.equalsTypeSubtype(contentMimeType))
                .findFirst()
                .map(bodyFormats::get)
                .orElse(defaultBodyFormat);
    }

    public Response<U> asError(int httpCode, U error) {
        setStatusCode(httpCode);
        setBody(error);
        return this;
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

    public void send() {
        var headers = exchange.getResponseHeaders();
        var body = bodyFormat.to(getBody());
        // TODO: Check Accept header instead of hardcoding UTF-8
        var bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        headers.set("Content-Type", bodyFormat.getMimeType().toString());

        // automatically closes stream
        try (var stream = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(statusCode, bodyBytes.length);
            stream.write(bodyBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send response");
        }
    }

}
