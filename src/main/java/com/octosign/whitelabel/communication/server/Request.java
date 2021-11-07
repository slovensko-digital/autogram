package com.octosign.whitelabel.communication.server;

import com.google.common.collect.ImmutableMap;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;

public class Request<T> {

    private final HttpExchange exchange;

    private final Map<MimeType, BodyFormat> BODY_FORMATS = ImmutableMap.of(
        MimeType.JSON, JSON,
        MimeType.PLAIN, JSON, // Plain is considered JSON so clients can prevent CORS preflight
        MimeType.ANY, JSON // Implicit default format
    );

    private final BodyFormat bodyFormat;

    private T body;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;

        try {
            bodyFormat = extractBodyFormat(exchange.getRequestHeaders());
        } catch (Exception ex) {
            throw new IntegrationException(Code.MALFORMED_INPUT, ex);
        }
    }

    public T getBody() { return body; }

    public HttpExchange getExchange() { return exchange; }

    public BodyFormat getBodyFormat() { return bodyFormat; }

    /**
     * List of supported body MIME types
     */
    public List<MimeType> getSupportedBodyFormats() {
        return new ArrayList<>(BODY_FORMATS.keySet());
    }

    /**
     * Retrieves and sets body as expected object
     *
     * Must be called only once
     *
     * @param bodyClass Class of the expected object in the body
     */
    //TODO userexception
    public T processBody(Class<T> bodyClass) {
        try {
            // TODO: Get charset from the Content-Type header - don't assume it's UTF-8
            var bodyString = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            body = bodyFormat.from(bodyString, bodyClass);
            return body;
        } catch (Exception e) {
            return null;
        }
    }

    public <U extends Map<String, List<String>>> BodyFormat extractBodyFormat(U source) {
        var contentType = source.get("Content-Type");
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
}
