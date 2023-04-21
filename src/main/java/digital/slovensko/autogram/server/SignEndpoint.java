package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.MalformedBodyException;
import digital.slovensko.autogram.server.dto.ErrorResponseBody;
import digital.slovensko.autogram.server.dto.SignRequestBody;

import java.io.IOException;

public class SignEndpoint implements HttpHandler {
    private final Autogram autogram;
    private final static Gson gson = new Gson();

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
            SigningJob job;
            try {
                var body = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), SignRequestBody.class);
                job = new SigningJob(body.getDocument(), body.getParameters(), new ServerResponder(exchange));

            } catch (JsonSyntaxException e) {
                new ServerResponder(exchange).onDocumentSignFailed(null, new MalformedBodyException(e.getMessage(), e));
                return;

            } catch (AutogramException e) {
                new ServerResponder(exchange).onDocumentSignFailed(null, e);
                return;
            }

            autogram.sign(job);

        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
            exchange.getResponseBody().write(gson.toJson(new ErrorResponseBody("INTERNAL_ERROR",
                    "Unexpected exception signing document", e.getMessage())).getBytes());
            exchange.getResponseBody().close();
            return;
        }
    }
}
