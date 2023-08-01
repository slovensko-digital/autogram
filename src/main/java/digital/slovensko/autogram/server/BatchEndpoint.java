package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.server.dto.BatchEndRequestBody;
import digital.slovensko.autogram.server.dto.BatchStartRequestBody;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

import java.io.IOException;

public class BatchEndpoint implements HttpHandler {
    private final Autogram autogram;

    public BatchEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        try {
            if (requestMethod.equalsIgnoreCase("POST")) {
                // Start batch
                var body = EndpointUtils.loadFromJsonExchange(exchange,
                        BatchStartRequestBody.class);
                autogram.batchStart(body.getTotalNumberOfDocuments(),
                        new BatchServerResponder(exchange));
            } else if (requestMethod.equalsIgnoreCase("DELETE")) {
                // End batch
                var body = EndpointUtils.loadFromJsonExchange(exchange,
                        BatchEndRequestBody.class);
                var finished = autogram.batchEnd(body.batchId());
                EndpointUtils.respondWith(finished ? new Object() {
                    public String status = "FINISHED";
                } : new Object() {
                    public String status = "NOT_FINISHED";
                }, exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (JsonSyntaxException e) {
            var response = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(response, exchange);
        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
