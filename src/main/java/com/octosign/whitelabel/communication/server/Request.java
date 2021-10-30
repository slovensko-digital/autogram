package com.octosign.whitelabel.communication.server;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.sun.net.httpserver.HttpExchange;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;
import static com.octosign.whitelabel.ui.Main.translate;

public class Request<T> {

    private final HttpExchange exchange;

    private final Map<MimeType, BodyFormat> bodyFormats = Map.of(
        MimeType.JSON, JSON,
        MimeType.PLAIN, JSON, // Plain is considered JSON so clients can prevent CORS preflight
        MimeType.ANY, JSON // Implicit default format
    );

    private final BodyFormat bodyFormat;

    private final QueryParams queryParams;

    private T body;

    public Request(HttpExchange exchange) throws IntegrationException {
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

            this.queryParams = QueryParams.parse(getExchange().getRequestURI().getQuery());
        } catch (Exception ex) {
            throw new IntegrationException(Code.MALFORMED_INPUT, translate("Invalid request/parsing error", ex));
        }
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    public BodyFormat getBodyFormat() { return bodyFormat; }

    public QueryParams getQueryParams() { return queryParams; }

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


    /**
     *
     * Inner class encapsulating logic for reading and handling query params
     *
     */
    public static class QueryParams {
        private final List<Param> params;

        static QueryParams parse(final String query) {
            return new QueryParams(query);
        }

        private QueryParams(final String query) {
            if (query == null || query.isEmpty()) {
                params = Collections.emptyList();
            } else {
                params = Stream.of(query.split("&"))
                        .filter(entry -> !entry.isEmpty())
                        .map(param -> new Param(param.split("=")))
                        .collect(Collectors.toList());
            }
        }

        public boolean isDefined(String name) {
            return params.stream().anyMatch(param -> param.name.equalsIgnoreCase(name));
        }

        public String get(String name) {
            Optional<Param> found = params.stream().filter(param -> param.name.equalsIgnoreCase(name)).findFirst();
            String result = null;

            if (found.isPresent())
                result = found.get().value;

            return result;
        }

        private record Param(String name, String value) {
            public Param(String[] pairOrSingle) {
                this(pairOrSingle[0], (pairOrSingle.length == 1) ? "" : pairOrSingle[1]);
            }
        }
    }
}
