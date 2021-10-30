package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
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
            verifyHTTPMethod(exchange);
            verifyOrigin(exchange);
            handleRequest(exchange);
        } catch (IntegrationException e) {
            var error = new CommunicationError(Code.UNEXPECTED_ERROR, translate("error.requestHandlingFailed"), e.getMessage());
            Response<CommunicationError> errorResponse = null;

            try {
                errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_INTERNAL_ERROR, error);
                errorResponse.send();
            } catch (Throwable t) {
                throw new RuntimeException(translate("error.responseFailed", errorResponse.getBody()));
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
        var error = new CommunicationError(Code.UNEXPECTED_ORIGIN, translate("error.unexpectedOrigin"));
        var errorResponse =
                new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_FORBIDDEN, error);

        // Don't allow remote address != localhost when listening on localhost
        var hostname = server.getDefaultHostname();
        var listeningOnLocalhost = hostname.equals("localhost") || hostname.equals("127.0.0.1");
        System.out.println(hostname);
        var isLoopbackAddress = exchange.getRemoteAddress().getAddress().isLoopbackAddress();
        System.out.println(isLoopbackAddress);
        if (listeningOnLocalhost && !isLoopbackAddress) {
            try {
                System.out.println("posielam z prvej");
                errorResponse.send();
            } catch (IOException e) {
                throw new IntegrationException(Code.UNEXPECTED_ORIGIN, translate("error.responseFailed"), e);
            }
            return;
        }

        var allowedOrigin = server.getAllowedOrigin();
        var originHeader = exchange.getRequestHeaders().get("Origin");

        System.out.println(originHeader);
        if (originHeader != null && !allowedOrigin.equals("*")) {
            var origin = originHeader.get(0);
            System.out.println(origin);
            if (!origin.equals(allowedOrigin)) {
                try {
                    System.out.println("posielam z 2");
                    errorResponse.send();
                } catch (IOException e) {
                    throw new IntegrationException(Code.UNEXPECTED_ORIGIN, translate("error.responseFailed"), e);
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
            var message = translate("error.unsupportedOperation", String.join(", ", allowedMethodsList));
            var error = new CommunicationError(Code.UNSUPPORTED_OPERATION, message);
            var errorResponse = new Response<CommunicationError>(exchange).asError(HttpURLConnection.HTTP_BAD_METHOD, error);

            try {
                errorResponse.send();
            } catch (IOException e) {
                throw new IntegrationException(Code.UNEXPECTED_ORIGIN, translate("error.responseFailed", message), e);
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
        if (response == null)
            throw new IntegrationException(Code.NULL_ARGUMENT, "Unable to send error response: response is null");

        try {
            response.send();
        } catch (IOException e) {
            throw new IntegrationException(Code.UNEXPECTED_ORIGIN, translate("error.responseFailed"), e);
        }
    }


    public <U> void rethrow(Response<U> response, TConsumer<Response<U>> responseProcessor) throws IntegrationException {
        var description = response.getBody();

        if (description == null)
            throw new IntegrationException(Code.MALFORMED_INPUT, "error.responseInvalid");

        try {
            responseProcessor.accept(response);
        } catch (IOException e) {
            throw new IntegrationException(Code.RESPONSE_FAILED, translate("error.responseFailed", description), e);
        }
    }

    public interface TConsumer<T> {
        void accept(T t) throws IOException;
    }
}
