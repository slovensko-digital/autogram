package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Server API endpoint without request body like GET or HEAD
 *
 * @param <U> Response body
 */
abstract class ReadEndpoint<U> extends Endpoint {

    public ReadEndpoint(Server server) {
        super(server);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IntegrationException {
        var request = new Request<>(exchange);
        var response = handleRequest(request, new Response<>(exchange));

        send(response);
    }

    /**
     * Handle request on this endpoint
     *
     * @param request   Request
     * @param request   Request
     * @param response  Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     * @throws IOException
     */
    protected abstract Response<U> handleRequest(Request<?> request, Response<U> response) throws IntegrationException;

    /**
     * Class of the response body object
     */
    protected abstract Class<U> getResponseClass();

}
