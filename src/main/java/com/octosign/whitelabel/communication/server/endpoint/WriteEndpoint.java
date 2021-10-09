package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server API endpoint
 *
 * TODO: Handle IOException in the handle - it means the request was aborted from the client
 * @param <Req> Expected request body
 * @param <Res> Response body
 */
abstract class WriteEndpoint<Req, Res> extends Endpoint {

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
    protected void handleRequest(HttpExchange exchange) throws IOException {
        var request = new Request<Req>(exchange);

        // TODO: Add verifying of HMAC if server has secretKey specified

        var requestClass = getRequestClass();
        if (requestClass != null) {
            // If request class is not null, the body must contain correct object
            if (request.getBodyFormat() == null) {
                var supportedTypes = request.getSupportedBodyFormats().stream()
                    .map(m -> m.toString())
                    .collect(Collectors.joining(", "));
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

        var response = handleRequest(request, new Response<Res>(exchange));
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
    protected abstract Response<Res> handleRequest(Request<Req> request, Response<Res> response) throws IOException;

    /**
     * Class of the request body object, should be null if request does not have body
     */
    protected abstract Class<Req> getRequestClass();

    /**
     * Class of the response body object
     */
    protected abstract Class<Res> getResponseClass();

    /**
     * Use response produced by the endpoint handler
     *
     * @param response
     * @throws IOException
     */
    protected void useResponse(Response<Res> response) throws IOException {
        if (response != null) {
            response.send();
            // TODO: Add bumping of nonce
        } else {
            // The request failed and the endpoint sent its own error response
            // TODO: Check response stream to make sure this is the case
        }
    }

}
