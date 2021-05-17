package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.ui.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Server API endpoint with no request or response abstraction
 *
 * When writing a new endpoint, consider using high-level ReadEndpoint or WriteEndpoint.
 */
abstract class Endpoint implements HttpHandler {

    // TODO Think about handling of IOException-s

    protected final Server server;

    public Endpoint(Server server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (verifyHTTPMethod(exchange) == false) return;
        if (verifyOrigin(exchange) == false) return;

        try {
            handleRequest(exchange);
        } catch (Throwable e) {
            var message = "An unexpected internal error occurred";
            var error = new CommunicationError(Code.UNEXPECTED_ERROR, message, e.getMessage());
            new Response<CommunicationError>(exchange)
                .asError(HttpURLConnection.HTTP_INTERNAL_ERROR, error)
                .send();
        }
    }

    /**
     * Handle request on this endpoint
     *
     * When writing a new endpoint, consider high-level Response<Res> handleRequest(request, response).
     */
    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    /**
     * List of allowed HTTP methods
     */
    protected abstract String[] getAllowedMethods();

    /**
     * Verifies the HTTP origin, remote address, and adds the origin to response
     *
     * @return True if valid, false if not
     * @throws IOException
     */
    protected boolean verifyOrigin(HttpExchange exchange) throws IOException {
        var message = "The request comes from an unexpected origin";
        var error = new CommunicationError(Code.UNEXPECTED_ORIGIN, message);
        var errorResponse = new Response<CommunicationError>(exchange)
            .asError(HttpURLConnection.HTTP_FORBIDDEN, error);

        // Don't allow remote address != localhost when listening on localhost
        var hostname = Main.getProperty("server.hostname");
        var listeningOnLocalhost = hostname.equals("localhost") || hostname.equals("127.0.0.1");
        if (listeningOnLocalhost && !exchange.getRemoteAddress().getAddress().isLoopbackAddress()) {
            errorResponse.send();
            return false;
        }

        var allowedOrigin = server.getAllowedOrigin();
        var originHeader = exchange.getRequestHeaders().get("Origin");
        if (originHeader != null && !allowedOrigin.equals("*")) {
            var origin = originHeader.get(0);
            if (!origin.equals(allowedOrigin)) {
                errorResponse.send();
                return false;
            }
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowedOrigin);

        return true;
    }

    /**
     * Verify the HTTP methods allowed
     *
     * Sends error response if not
     *
     * @param exchange
     * @return True if valid, false if not
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

}
