package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.sun.net.httpserver.HttpExchange;

/**
 * Server API endpoint
 *
 * TODO: Handle IOException in the handle - it means the request was aborted from the client
 * @param <T> Expected request body
 * @param <U> Response body
 */
abstract class WriteEndpoint<T, U> extends Endpoint {

    // Endpoint's current nonce, incremented on each successful request.
    private AtomicInteger nonce;

    public WriteEndpoint(Server server, int initialNonce) {
        super(server);

        nonce = new AtomicInteger(initialNonce);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        var request = new Request<T>(exchange);

        // TODO: Add verifying of HMAC if server has secretKey specified

        var requestClass = getRequestClass();
        if (requestClass != null) {
            // If request class is not null, the body must contain correct object
            if (request.getBodyFormat() == null) {
                var supportedTypes = String.join(", ", request.getSupportedBodyFormats());
                var message = "Unsupported request body MIME type. Supported: " + supportedTypes;
                var error = new CommunicationError(Code.UNSUPPORTED_FORMAT, message);
                new Response<CommunicationError>(exchange)
                    .asError(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, error)
                    .send();
                return;
            } else if (request.processBody(requestClass) == null) {
                var error = new CommunicationError(Code.MALFORMED_INPUT, "Malformed input body.");
                new Response<CommunicationError>(exchange)
                    .asError(HttpURLConnection.HTTP_BAD_REQUEST, error)
                    .send();
                return;
            }
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        var response = handleRequest(request, new Response<U>(exchange));
        useResponse(response);
    }

    /**
     * Handle request on this endpoint
     *
     * @param request   Request
     * @param response  Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     * @throws IOException
     */
    protected abstract Response<U> handleRequest(Request<T> request, Response<U> response) throws IOException;

    /**
     * Class of the request body object, should be null if request does not have body
     */
    protected abstract Class<T> getRequestClass();

    /**
     * Class of the response body object
     */
    protected abstract Class<U> getResponseClass();

    /**
     * Use response produced by the endpoint handler
     *
     * @param response
     * @throws IOException
     */
    protected void useResponse(Response<U> response) throws IOException {
        if (response != null) {
            response.send();
            // TODO: Add bumping of nonce
        } else {
            // The request failed and the endpoint sent its own error response
            // TODO: Check response stream to make sure this is the case
        }
    }

}
