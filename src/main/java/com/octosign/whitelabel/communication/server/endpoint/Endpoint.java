package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.util.Arrays;

import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.ui.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import static com.octosign.whitelabel.error_handling.Code.UNEXPECTED_ERROR;

/**
 * Server API endpoint with no request or response abstraction
 *
 * When writing a new endpoint, consider using high-level ReadEndpoint or WriteEndpoint.
 */
abstract class Endpoint implements HttpHandler {

    protected final Server server;

    public Endpoint(Server server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) {
        Utils.setExchange(exchange);
        try {
            if (verifyHTTPMethod(exchange) && verifyOrigin(exchange))
                handleRequest(exchange);
        } catch (UserException e) {
            throw e;
        } catch (IntegrationException e) {
            new Response<IntegrationException>(exchange)
                .asError(e.getCode().toHttpCode(), e)
                .send();
            throw e;
        } catch (Throwable t) {
            var error = new IntegrationException(UNEXPECTED_ERROR, t);

            new Response<Throwable>(exchange)
                .asError(error.getCode().toHttpCode(), error)
                .send();
            throw new UnknownError(t.toString());
        }
    }

    /**
     * Handle request on this endpoint
     *
     * When writing a new endpoint, consider high-level Response<U> handleRequest(request, response).
     */
    protected abstract void handleRequest(HttpExchange exchange) throws Throwable;

    /**
     * List of allowed HTTP methods
     */
    protected abstract String[] getAllowedMethods();

    /**
     * Verifies the HTTP origin, remote address, and adds the origin to response
     *
     * @return True if valid, false if not
     */
    protected boolean verifyOrigin(HttpExchange exchange) {
        var hostname = server.getAddress().getHostName();
        var listeningOnLocalhost = hostname.equals("localhost") || hostname.equals("127.0.0.1");
        var isLoopbackAddress = exchange.getRemoteAddress().getAddress().isLoopbackAddress();

        if (listeningOnLocalhost && !isLoopbackAddress)
            throw new IntegrationException(Code.UNEXPECTED_ORIGIN);


        var allowedOrigin = server.getAllowedOrigin();
        var originHeader = exchange.getRequestHeaders().get("Origin");

        if (originHeader != null && !allowedOrigin.equals("*")) {
            var origin = originHeader.get(0);

            if (!origin.equals(allowedOrigin))
                throw new IntegrationException(Code.UNEXPECTED_ORIGIN);
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
    protected boolean verifyHTTPMethod(HttpExchange exchange) {
        var allowedMethods = Arrays.asList(getAllowedMethods());

        if (!allowedMethods.contains(exchange.getRequestMethod())) {
            throw new IntegrationException(Code.UNSUPPORTED_OPERATION, "Allowed methods: " + String.join(", ", allowedMethods));
        }
        return true;
    }
}
