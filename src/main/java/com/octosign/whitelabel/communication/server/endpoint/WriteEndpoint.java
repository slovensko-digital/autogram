package com.octosign.whitelabel.communication.server.endpoint;

import java.util.concurrent.atomic.AtomicInteger;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.*;
import com.octosign.whitelabel.error_handling.*;
import com.sun.net.httpserver.HttpExchange;

/**
 * Server API endpoint
 *
 * @param <T> Expected request body
 * @param <U> Response body
 */
abstract class WriteEndpoint<T, U> extends Endpoint {

    /**
     * This endpoint's current nonce
     *
     * Incremented on each successful request.
     */
    private AtomicInteger nonce;

    public WriteEndpoint(Server server, int initialNonce) {
        super(server);

        nonce = new AtomicInteger(initialNonce);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws Throwable {
        var request = new Request<T>(exchange);

        // TODO: Add verifying of HMAC if server has secretKey specified

        var requestClass = getRequestClass();
        if (requestClass != null) {
            // If request class is not null, the body must contain correct object
            if (request.getBodyFormat() == null) {
                var supportedTypes = String.join(", ", request.getSupportedBodyFormats().stream().map(MimeType::toString).toArray(String[]::new));
                throw new IntegrationException(Code.UNSUPPORTED_FORMAT, "Unsupported format. Supported formats are: " + supportedTypes);
            }

            if (request.processBody(getRequestClass()) == null) {
                throw new IntegrationException(Code.MALFORMED_INPUT);
            }
        }
        var response = handleRequest(request, new Response<>(exchange));
        response.send();
        nonce.incrementAndGet();
    }

    /**
     * Handle request on this endpoint
     *
     * @param request   Request
     * @param response  Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     */
    protected abstract Response<U> handleRequest(Request<T> request, Response<U> response)
            throws Throwable;

    /**
     * Class of the request body object, should be null if request does not have body
     */
    protected abstract Class<T> getRequestClass();

    /**
     * Class of the response body object
     */
    protected abstract Class<U> getResponseClass();
}
