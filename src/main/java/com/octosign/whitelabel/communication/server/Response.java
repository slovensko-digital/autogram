package com.octosign.whitelabel.communication.server;

import com.octosign.whitelabel.communication.server.format.BodyFormat;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.octosign.whitelabel.communication.server.format.StandardBodyFormats.JSON;
import static com.octosign.whitelabel.ui.Main.translate;

/**
 * Server response that conforms to request headers
 */
public class Response<U> {

    private final HttpExchange exchange;

    private final Map<String, BodyFormat> bodyFormats;

    private final BodyFormat bodyFormat;

    private int statusCode = HttpURLConnection.HTTP_OK;

    private U body;

    public Response(HttpExchange exchange) throws IntegrationException {
        this.exchange = exchange;

        try {
            bodyFormats = Map.of(
                    JSON.getMimeType(), JSON,
                    "*/*", JSON // Default format
            );

            var contentType = exchange.getRequestHeaders().get("Accept");
            bodyFormat = contentType.stream()
                    .map((mimeType) -> mimeType.split(";")[0].toLowerCase())
                    .filter(bodyFormats::containsKey)
                    .findFirst()
                    .map(bodyFormats::get)
                    // We would rather return something in unaccepted type than nothing
                    .orElseGet(() -> bodyFormats.get("*/*"));
        } catch (Exception ex) {
            throw new IntegrationException(Code.MALFORMED_INPUT, translate("Invalid request/parsing error", ex));
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

        headers.set("Content-Type", "application/json; charset=UTF-8");

        // automatically closes stream
        try (var stream = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(statusCode, bodyBytes.length);
            stream.write(bodyBytes);
        }
//        catch (IOException e) {
//            throw new IntegrationException(Code.RESPONSE_FAILED, translate("error.responseFailed", body));
//        }
    }
}
