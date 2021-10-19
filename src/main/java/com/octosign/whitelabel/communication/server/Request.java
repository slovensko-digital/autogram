package com.octosign.whitelabel.communication.server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.sun.net.httpserver.HttpExchange;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;

public class Request<T> {

    private final HttpExchange exchange;

    private final Map<MimeType, BodyFormat> bodyFormats = Map.of(
        MimeType.JSON, JSON,
        MimeType.PLAIN, JSON, // Plain is considered JSON so clients can prevent CORS preflight
        MimeType.ANY, JSON // Implicit default format
    );

    private final BodyFormat bodyFormat;

    private T body;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;

        var contentType = exchange.getRequestHeaders().get("Content-Type");
        if (contentType != null) {
            var contentMimeType = MimeType.parse(contentType.get(0));
            bodyFormat = bodyFormats.keySet().stream()
                .filter(m -> m.equalsTypeSubtype(contentMimeType))
                .findFirst()
                .map(m -> bodyFormats.get(m))
                .orElse(bodyFormats.get(MimeType.ANY));
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
    public List<MimeType> getSupportedBodyFormats() {
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
