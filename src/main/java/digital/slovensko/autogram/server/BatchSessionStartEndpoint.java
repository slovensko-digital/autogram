package digital.slovensko.autogram.server;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.server.dto.BatchSessionStartRequestBody;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

public class BatchSessionStartEndpoint implements HttpHandler {
    private final Autogram autogram;

    public BatchSessionStartEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Allow preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, BatchSessionStartRequestBody.class);
            autogram.batchStart(body.getTotalNumberOfDocuments(), new ServerBatchStartResponder(exchange));
        } catch (JsonSyntaxException e) {
            var errorResponse = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(errorResponse, exchange);
        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
            e.printStackTrace();
        }
    }
}
