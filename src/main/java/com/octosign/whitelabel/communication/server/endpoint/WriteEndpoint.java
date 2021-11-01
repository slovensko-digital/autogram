package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.octosign.whitelabel.ui.Main.getProperty;
import static com.octosign.whitelabel.ui.Main.translate;

/**
 * Server API endpoint
 *
 * TODO: Handle IOException in the handle - it means the request was aborted from the client
 * @param <Q> Expected request body
 * @param <S> Response body
 */
abstract class WriteEndpoint<Q,S> extends Endpoint {

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
    protected void handleRequest(HttpExchange exchange) throws IntegrationException {
        var request = new Request<Q>(exchange);

        // TODO: Add verifying of HMAC if server has secretKey specified

        var requestClass = getRequestClass();
        if (requestClass != null) {
            // If request class is not null, the body must contain correct object
            Response<CommunicationError> errorResponse;

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
                var error = new CommunicationError(Code.MALFORMED_INPUT, getProperty("exc.malformedInput"));
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_REQUEST, error);
            } else
                return;
            }

            if (request.processBody(requestClass) == null) {
                var error = new CommunicationError(Code.MALFORMED_INPUT, getProperty("error.malformedInput"));
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_REQUEST, error);

                send(errorResponse);
                return;
            }
        }

        var response = handleRequest(request, new Response<>(exchange));
        send(response);
    }

    /**
     * Handle request on this endpoint
     *
     * @param request   Request
     * @param response  Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     * @throws IOException
     */
    protected abstract Response<S> handleRequest(Request<Q> request, Response<S> response) throws IntegrationException;

    /**
     * Class of the request body object, should be null if request does not have body
     */
    protected abstract Class<Q> getRequestClass();

    /**
     * Class of the response body object
     */
    protected abstract Class<S> getResponseClass();
}
