package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.ValidateRequestBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

import static digital.slovensko.autogram.util.DSSUtils.getDocumentValidator;

import java.io.IOException;

public class ValidateEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        try {
            if (requestMethod.equalsIgnoreCase("POST")) {
                var body = EndpointUtils.loadFromJsonExchange(exchange, ValidateRequestBody.class);

                var doc = body.getDocument();
                var report = SignatureValidator.getInstance().validate(getDocumentValidator(doc));


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
