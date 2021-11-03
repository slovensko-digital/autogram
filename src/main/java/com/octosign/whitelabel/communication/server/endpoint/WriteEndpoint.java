package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.octosign.whitelabel.ui.I18n.translate;

/**
 * Server API endpoint
 * <p>
 * TODO: Handle IOException in the handle - it means the request was aborted from the client
 *
 * @param <T> Expected request body
 * @param <U> Response body
 */
abstract class WriteEndpoint<T, U> extends Endpoint {

    /**
     * This endpoint's current nonce
     * <p>
     * Incremented on each successful request.
     */
    private AtomicInteger nonce;

    public WriteEndpoint(Server server, int initialNonce) {
        super(server);
        nonce = new AtomicInteger(initialNonce);
    }

    // TODO: Add verifying of HMAC if server has secretKey specified
    @Override
    protected void handleRequest(HttpExchange exchange) {
        var request = new Request<T>(exchange);

        if (getRequestClass() != null) {
            System.out.println(getRequestClass());
            System.out.println(request.getBodyFormat());

            if (request.getBodyFormat() == null) {
                var supportedTypes = String.join(", ", request.getSupportedBodyFormats().stream().map(MimeType::toString).toArray(String[]::new));
                System.out.println(supportedTypes);

                throw new IntegrationException(Code.BAD_REQUEST, translate("error.invalidMimetype_", supportedTypes));
            }

            if (request.processBody(getRequestClass()) == null) {
                throw new IntegrationException(Code.MALFORMED_INPUT, translate("error.malformedInput"));
            }
        }

        var response = handleRequest(request, new Response<>(exchange));
        useResponse(response);
    }

    /**
     * Handle request on this endpoint
     *
     * @param request  Request
     * @param response Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     * @throws IOException
     */
    protected abstract Response<U> handleRequest(Request<T> request, Response<U> response);

    /**
     * Class of the request body object, should be null if request does not have body
     */
    protected abstract Class<T> getRequestClass();

    /**
     * Class of the response body object
     */
    protected abstract Class<U> getResponseClass();
}
