package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.XMLValidator;
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
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Allow preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, SignRequestBody.class);
            var job = new SigningJob(body.getDocument(), body.getParameters(), new ServerResponder(exchange));

            XMLValidator xmlValidator = new XMLValidator(body);
            xmlValidator.validate();

            autogram.sign(job);
        } catch (JsonSyntaxException e) {
            var response = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(response, exchange);
        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
