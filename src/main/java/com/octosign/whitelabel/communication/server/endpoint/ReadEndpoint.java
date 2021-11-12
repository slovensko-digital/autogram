package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;

import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.sun.net.httpserver.HttpExchange;

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
    protected void handleRequest(HttpExchange exchange) {
        var request = new Request<>(exchange);

        var response = handleRequest(request, new Response<U>(exchange));
        useResponse(response);
    }

    /**
     * Handle request on this endpoint
     *
     * @param request   Request
     * @param response  Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     */
    protected abstract Response<U> handleRequest(Request<?> request, Response<U> response);

    /**
     * Class of the response body object
     */
    protected abstract Class<U> getResponseClass();

}
