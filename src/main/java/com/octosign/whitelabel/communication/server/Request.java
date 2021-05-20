package com.octosign.whitelabel.communication.server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.sun.net.httpserver.HttpExchange;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;

public class Request<T> {

    private final HttpExchange exchange;

    private final Map<String, BodyFormat> bodyFormats;

    private final BodyFormat bodyFormat;

    private T body;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;

        bodyFormats = Map.of(
            JSON.getMimeType(), JSON,
            "text/plain", JSON, // Considered JSON so clients can prevent CORS preflight
            "*/*", JSON // Implicit default format
        );

        var contentType = exchange.getRequestHeaders().get("Content-Type");
        if (contentType != null) {
            bodyFormat = contentType.stream()
                .map((mimeType) -> mimeType.split(";")[0].toLowerCase())
                .filter((String mimeType) -> bodyFormats.containsKey(mimeType))
                .findFirst()
                .map((mimeType) -> bodyFormats.get(mimeType))
                .orElseGet(() -> contentType.isEmpty() ? bodyFormats.get("*/*") : null);
        } else {
            bodyFormat = null;
        }
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    public BodyFormat getBodyFormat() {
        return bodyFormat;
    }

    /**
     * List of supported body MIME types
     */
    public List<String> getSupportedBodyFormats() {
        return new ArrayList<>(bodyFormats.keySet());
    }

    public T getBody() {
        return body;
    }

    /**
     * Retrieves and sets body as expected object
     *
     * Must be called only once
     *
     * @param <T>       Expected object in the body
     * @param bodyClass Class of the expected object in the body
     */
    public T processBody(Class<T> bodyClass) {
        var stream = exchange.getRequestBody();

        try {
            // TODO: Get charset from the Content-Type header - don't assume it's UTF-8
            var bodyString = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            body = bodyFormat.from(bodyString, bodyClass);
            return body;
        } catch (Exception e) {
            return null;
        }
    }

}
