package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.ui.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.octosign.whitelabel.ui.Main.getProperty;

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
                var supportedTypes = String.join(", ", request.getSupportedBodyFormats());
                var error = new CommunicationError(Code.UNSUPPORTED_FORMAT, getProperty("exc.invalidMimeType", supportedTypes));
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, error);
            } else if (request.processBody(requestClass) == null) {
                var error = new CommunicationError(Code.MALFORMED_INPUT, getProperty("exc.malformedInput"));
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_REQUEST, error);
            } else
                return;

            try {
                errorResponse.send();
            } catch(IOException e) {
                throw new IntegrationException(String.format("Unable to send response: %s", e.getMessage()));
            }
        }

        var response = handleRequest(request, new Response<S>(exchange));
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
    protected abstract Response<S> handleRequest(Request<Q> request, Response<S> response) throws IntegrationException;

    /**
     * Class of the request body object, should be null if request does not have body
     */
    protected abstract Class<Q> getRequestClass();

    /**
     * Class of the response body object
     */
    protected abstract Class<S> getResponseClass();

//    /**
//     * Use response produced by the endpoint handler
//     *
//     * @param response
//     * @throws IOException
//     */
//    protected void useResponse(Response<S> response) throws IOException {
//        if (response != null) {
//            response.send();
//            // TODO: Add bumping of nonce
//        } else {
//            // The request failed and the endpoint sent its own error response
//            // TODO: Check response stream to make sure this is the case
//        }
//    }

}
