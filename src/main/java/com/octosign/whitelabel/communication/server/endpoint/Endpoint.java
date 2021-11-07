package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Arrays;

import static com.octosign.whitelabel.ui.I18n.translate;

/**
 * Server API endpoint with no request or response abstraction
 * <p>
 * When writing a new endpoint, consider using high-level ReadEndpoint or WriteEndpoint.
 */
abstract class Endpoint implements HttpHandler {

    protected final Server server;

    public Endpoint(Server server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (verifyHTTPMethod(exchange) && verifyOrigin(exchange))
                handleRequest(exchange);

        } catch (IntegrationException e) {
            throw new IntegrationException(Code.HTTP_EXCHANGE_FAILED, e);
        }
    }

    /**
     * Handle request on this endpoint
     * <p>
     * When writing a new endpoint, consider high-level Response<Res> handleRequest(request, response).
     */
    protected abstract void handleRequest(HttpExchange exchange);

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
        var hostname = server.getHostname();
        var listeningOnLocalhost = hostname.equals("localhost") || hostname.equals("127.0.0.1");
        var isLoopbackAddress = exchange.getRemoteAddress().getAddress().isLoopbackAddress();

        if (listeningOnLocalhost && !isLoopbackAddress)
            throw new IntegrationException(Code.UNEXPECTED_ORIGIN, translate("error.unexpectedOrigin"));


        var allowedOrigin = server.getAllowedOrigin();
        var originHeader = exchange.getRequestHeaders().get("Origin");

        if (originHeader != null && !allowedOrigin.equals("*")) {
            var origin = originHeader.get(0);

            if (!origin.equals(allowedOrigin))
                throw new IntegrationException(Code.UNEXPECTED_ORIGIN, translate("error.unexpectedOrigin"));

        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        return true;
    }

    /**
     * Verify the HTTP methods allowed
     * <p>
     * Sends error response if not
     *
     * @param exchange
     * @return True if valid, false if not
     * @throws IOException
     */
    protected boolean verifyHTTPMethod(HttpExchange exchange) {
        var allowedMethods = Arrays.asList(getAllowedMethods());

        if (!allowedMethods.contains(exchange.getRequestMethod())) {
            var message = translate("error.unsupportedOperation", String.join(", ", allowedMethods));
            throw new IntegrationException(Code.UNSUPPORTED_OPERATION, message);
        }

        return true;
    }

    /**
     * Use response produced by the endpoint handler
     *
     * @param response
     */
    public <U> void useResponse(Response<U> response) {
        rethrow(response, Response::send);
    }

    public <T, U extends IOException> void rethrow(T subject, TConsumer<T, U> processor) {
        if (subject == null)
            throw new IntegrationException(Code.MALFORMED_INPUT, "error.responseInvalid");
        try {
            processor.accept(subject);
        } catch (IOException e) {
            throw new IntegrationException(Code.RESPONSE_FAILED, translate("error.responseFailed", subject), e);
        }
    }

    public interface TConsumer<T, U extends Exception> {
        void accept(T t) throws U;
    }
}
