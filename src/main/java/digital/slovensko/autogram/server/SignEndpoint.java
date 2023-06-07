package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

import java.io.IOException;

public class SignEndpoint implements HttpHandler {
    private final Autogram autogram;

    public SignEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, SignRequestBody.class);
            var job = new SigningJob(body.getDocument(), body.getParameters(),
                    new ServerResponder(exchange));
            if (body.getBatchId() != null) {
                autogram.batchSign(job, body.getBatchId());
            } else {
                autogram.sign(job);
            }
        } catch (JsonSyntaxException e) {
            var response =
                    ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(response, exchange);
        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
