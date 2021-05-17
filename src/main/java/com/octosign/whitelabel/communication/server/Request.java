package com.octosign.whitelabel.communication.server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.octosign.whitelabel.communication.server.format.JsonFormat;
import com.sun.net.httpserver.HttpExchange;

public class Request<T> {

    private final HttpExchange exchange;

    private final Map<String, BodyFormat> bodyFormats;

    private final BodyFormat bodyFormat;

    private T body;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;

        this.bodyFormats = Map.of(
            JsonFormat.MIME_TYPE, new JsonFormat(),
            "text/plain", new JsonFormat(), // Considered JSON so clients can prevent CORS preflight
            "*/*", new JsonFormat() // Implicit default format
        );

        var contentType = exchange.getRequestHeaders().get("Content-Type");
        if (contentType != null) {
            this.bodyFormat = contentType.stream()
                .map((mimeType) -> mimeType.split(";")[0].toLowerCase())
                .filter((String mimeType) -> this.bodyFormats.containsKey(mimeType))
                .findFirst()
                .map((mimeType) -> this.bodyFormats.get(mimeType))
                .orElseGet(() -> contentType.isEmpty() ? this.bodyFormats.get("*/*") : null);
        } else {
            this.bodyFormat = null;
        }
    }

    public HttpExchange getExchange() {
        return this.exchange;
    }

    public BodyFormat getBodyFormat() {
        return this.bodyFormat;
    }

    /**
     * List of supported body MIME types
     */
    public List<String> getSupportedBodyFormats() {
        return new ArrayList<>(this.bodyFormats.keySet());
    }

    public T getBody() {
        return this.body;
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
        var stream = this.exchange.getRequestBody();

        try {
            // TODO: Get charset from the Content-Type header - don't assume it's UTF-8
            var bodyString = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            this.body = this.bodyFormat.from(bodyString, bodyClass);
            return this.body;
        } catch (Exception e) {
            return null;
        }
    }

}
