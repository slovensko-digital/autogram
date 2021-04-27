package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Server API endpoint with no request or response abstraction
 *
 * Consider using chikld ReadEndpoint or WriteEndpoint
 */
abstract class Endpoint implements HttpHandler {

    // TODO: Add verifyOrigin (check origin and remote address - the latter on localhost only)

    // TODO: Consider global try-catch over the handler sending back 500

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
     * List of allowed HTTP methods
     */
    protected abstract String[] getAllowedMethods();

}
