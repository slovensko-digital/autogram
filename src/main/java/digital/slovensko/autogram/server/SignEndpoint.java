package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.Autogram;
import digital.slovensko.autogram.core.SigningJob;
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

        var body = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), SignRequestBody.class);

        var job = new SigningJob(body.getDocument(), body.getParameters(), new ServerResponder(exchange));
        autogram.sign(job);
    }
}
