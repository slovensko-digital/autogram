package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.sun.net.httpserver.HttpExchange;

/**
 * Server API endpoint without request body like GET or HEAD
 *
 * @param <Res> Response body
 */
abstract class ReadEndpoint<Res> extends Endpoint {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO: Add checking of origin
        // TODO: Add checking of remote address (if listening on localhost only)
        if (verifyHTTPMethod(exchange) == false) return;

        var request = new Request<>(exchange);

        var response = handleRequest(request, new Response<Res>(exchange));
        useResponse(response);
    }

    /**
     * Verify the HTTP methods allowed
     *
     * Sends error response if not
     *
     * @param exchange
     * @return True if yes, false if not
     * @throws IOException
     */
    protected boolean verifyHTTPMethod(HttpExchange exchange) throws IOException {
        var allowedMethodsList = Arrays.asList(getAllowedMethods());
        if (!allowedMethodsList.contains(exchange.getRequestMethod())) {
            var supportedMethods = String.join(", ", allowedMethodsList);
            var message = "Unsupported HTTP method. Supported: " + supportedMethods;
            var error = new CommunicationError(Code.UNSUPPORTED_OPERATION, message);
            new Response<CommunicationError>(exchange)
                .asError(HttpURLConnection.HTTP_BAD_METHOD, error)
                .send();
            return false;
        }
        return true;
    }

    /**
     * Use response produced by the endpoint handler
     *
     * @param response
     * @throws IOException
     */
    protected void useResponse(Response<Res> response) throws IOException {
        if (response != null) {
            response.send();
        } else {
            // The request failed and the enpoint sent its own error response
            // TODO: Check response stream to make sure this is the case
        }
    }

    /**
     * Handle request on this endpoint
     *
     * @param request   Request
     * @param response  Prepared successful response
     * @return Modified response if the request succeeded or null if not and custom response was sent.
     * @throws IOException
     */
    protected abstract Response<Res> handleRequest(Request<?> request, Response<Res> response) throws IOException;

    /**
     * Class of the response body object
     */
    protected abstract Class<Res> getResponseClass();

    /**
     * List of allowed HTTP methods
     */
    protected abstract String[] getAllowedMethods();

}
