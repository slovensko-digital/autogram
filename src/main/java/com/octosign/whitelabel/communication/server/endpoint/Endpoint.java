package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.ui.IntegrationException;
import com.octosign.whitelabel.ui.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

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
    public void handle(HttpExchange exchange) {
        try {
            verifyHTTPMethod(exchange);
            verifyOrigin(exchange);
            handleRequest(exchange);
        } catch (IntegrationException e) {
            var error = new CommunicationError(Code.UNEXPECTED_ERROR, Main.getProperty("error.requestHandlingFailed"), e.getMessage());
            Response<CommunicationError> errorResponse = null;
            try {
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_INTERNAL_ERROR, error);
                errorResponse.send();
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Unable to send error response: %s", errorResponse.getBody()));
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
     * @throws IOException
     */
    protected void verifyOrigin(HttpExchange exchange) throws IntegrationException {
        var error = new CommunicationError(Code.UNEXPECTED_ORIGIN, Main.getProperty("error.unexpectedOrigin"));
        var errorResponse =
                new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_FORBIDDEN, error);

        // Don't allow remote address != localhost when listening on localhost
        var hostname = Main.getProperty("server.hostname");
        var listeningOnLocalhost = hostname.equals("localhost") || hostname.equals("127.0.0.1");
        if (listeningOnLocalhost && !exchange.getRemoteAddress().getAddress().isLoopbackAddress()) {
            try {
                errorResponse.send();
            } catch (IOException e) {
                throw new IntegrationException(String.format("Unable to send error response: %s", Main.getProperty("error.unexpectedOrigin")));
            }
            return;
        }

        var allowedOrigin = server.getAllowedOrigin();
        var originHeader = exchange.getRequestHeaders().get("Origin");
        if (originHeader != null && !allowedOrigin.equals("*")) {
            var origin = originHeader.get(0);
            if (!origin.equals(allowedOrigin)) {
                try {
                    errorResponse.send();
                } catch (IOException e) {
                    throw new IntegrationException(String.format("Unable to send error response: %s", Main.getProperty("error.unexpectedOrigin")));
                }
                return;
            }
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
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
    protected void verifyHTTPMethod(HttpExchange exchange) throws IntegrationException {
        var allowedMethodsList = Arrays.asList(getAllowedMethods());
        var requestMethod = exchange.getRequestMethod();

        if (!allowedMethodsList.contains(requestMethod)) {
            var message = Main.getProperty("error.unsupportedOperation", String.join(", ", allowedMethodsList));
            var error = new CommunicationError(Code.UNSUPPORTED_OPERATION, message);
            var response = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_METHOD, error);
            try {
                response.send();
            } catch (IOException e) {
                throw new IntegrationException(String.format("Unable to send error response: %s", requestMethod, message));
            }
        }
    }

    /**
     * Use response produced by the endpoint handler
     *
     * @param response
     * @throws IOException
     */
    protected <U> void useResponse(Response<U> response) throws IntegrationException {
        if (response != null) {
            try {
                response.send();
            } catch (IOException e) {
                throw new IntegrationException(String.format("Unable to send error response: %s",
                        Main.getProperty(String.format("error.unexpectedOrigin", response.getBody()))));
            }
        } else {
            throw new IntegrationException("Unable to send error response: response is null");
        }
    }

    public <U> void rethrow(Response<U> response, TConsumer<Response<U>> consumer) throws IntegrationException {
        var description = response.getBody();

        if (description == null)
            throw new IllegalStateException("Error with empty body");

        try {
            consumer.accept(response);
        } catch (IOException e) {
            throw new IntegrationException(String.format("Unable to send error response \"%s\". Cause: %s", description, e));
        }
    }

    public interface TConsumer<T> {
        void accept(T t) throws IOException;
    }
}
