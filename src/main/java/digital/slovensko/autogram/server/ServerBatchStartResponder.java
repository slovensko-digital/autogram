package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.server.dto.BatchSessionStartResponseBody;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

public class ServerBatchStartResponder {
    private final HttpExchange exchange;

    public ServerBatchStartResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public void onBatchStartSuccess(String batchId) {
        var gson = new Gson();

        try {
            var response = new BatchSessionStartResponseBody(batchId);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();
        } catch (JsonSyntaxException e) {
            var errorResponse = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(errorResponse, exchange);
        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }

    public void onBatchStartFailure(AutogramException error) {
        EndpointUtils.respondWithError(ErrorResponse.buildFromException(error), exchange);
    }
}
