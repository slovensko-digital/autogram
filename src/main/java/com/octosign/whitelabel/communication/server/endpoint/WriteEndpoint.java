package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.octosign.whitelabel.ui.I18n.translate;

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
                var supportedTypes = String.join(", ", request.getSupportedBodyFormats().stream().map(MimeType::toString).toArray(String[]::new));
                var error = new CommunicationError(Code.UNSUPPORTED_FORMAT, translate("error.invalidMimeType", supportedTypes));
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, error);

                try {
                    errorResponse.send();
                } catch (IOException e) {
                    throw new IntegrationException(Code.RESPONSE_FAILED, translate("error.responseFailed", e));
                }
                return;
            }

            if (request.processBody(requestClass) == null) {
                var error = new CommunicationError(Code.MALFORMED_INPUT, translate("error.malformedInput"));
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_REQUEST, error);

                try {
                    errorResponse.send();
                } catch (IOException e) {
                    throw new IntegrationException(Code.RESPONSE_FAILED, translate("error.responseFailed", e));
                }
                return;
            }
        }

        var response = handleRequest(request, new Response<>(exchange));
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
}
