package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.SignerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import static com.octosign.whitelabel.ui.I18n.translate;

/**
 * Server API endpoint with no request or response abstraction
 * <p>
 * When writing a new endpoint, consider using high-level ReadEndpoint or WriteEndpoint.
 */
abstract class Endpoint implements HttpHandler {

    // TODO Think about handling of IOException-s

    protected final Server server;

    public Endpoint(Server server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (verifyHTTPMethod(exchange) && verifyOrigin(exchange)) {
                handleRequest(exchange);
            }
        } catch (IntegrationException e) {
            var error = new CommunicationError(Code.UNEXPECTED_ERROR, translate("error.requestHandlingFailed"), e.getMessage());
            var errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_INTERNAL_ERROR, error);
            try {
                send(errorResponse);
            } catch (IntegrationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Handle request on this endpoint
     * <p>
     * When writing a new endpoint, consider high-level Response<Res> handleRequest(request, response).
     */
    protected abstract void handleRequest(HttpExchange exchange) throws IntegrationException;

    /**
     * List of allowed HTTP methods
     */
    protected abstract String[] getAllowedMethods();

    /**
     * Verifies the HTTP origin, remote address, and adds the origin to response
     *
     * @return True if valid, false if not
     * @throws IntegrationException
     */
    protected boolean verifyOrigin(HttpExchange exchange) throws IntegrationException {
        var error = new CommunicationError(Code.UNEXPECTED_ORIGIN, translate("error.unexpectedOrigin"));
        var errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_FORBIDDEN, error);

        var hostname = server.getDefaultHostname();
        var listeningOnLocalhost = hostname.equals("localhost") || hostname.equals("127.0.0.1");
        var isLoopbackAddress = exchange.getRemoteAddress().getAddress().isLoopbackAddress();
        if (listeningOnLocalhost && !isLoopbackAddress) {
            send(errorResponse);
            return false;
        }

        var allowedOrigin = server.getAllowedOrigin();
        var originHeader = exchange.getRequestHeaders().get("Origin");

        if (originHeader != null && !allowedOrigin.equals("*")) {
            var origin = originHeader.get(0);

            if (!origin.equals(allowedOrigin)) {
                send(errorResponse);
                return false;
            }
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
    protected boolean verifyHTTPMethod(HttpExchange exchange) throws IntegrationException {
        var allowedMethodsList = Arrays.asList(getAllowedMethods());
        if (allowedMethodsList.contains(exchange.getRequestMethod()))
            return true;

        var message = translate("error.unsupportedOperation", String.join(", ", allowedMethodsList));
        var error = new CommunicationError(Code.UNSUPPORTED_OPERATION, message);
        var errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_METHOD, error);

        send(errorResponse);
        return false;
    }

    /**
     * Use response produced by the endpoint handler
     *
     * @param response
     * @throws IOException
     */
    public <U> void send(Response<U> response) throws IntegrationException {
        rethrow(response, Response::send);
    }

    public <T, U extends IOException> void rethrow(T subject, TConsumer<T, U> processor) throws IntegrationException {
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
