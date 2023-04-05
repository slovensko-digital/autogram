package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.MalformedMimetypeException;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import eu.europa.esig.dss.model.InMemoryDocument;

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

        System.out.println("Received a HTTP sign request");

        Gson gson = new Gson();
        SignRequestBody body;
        InMemoryDocument document;
        SigningParameters parameters;

        try {
            body = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), SignRequestBody.class);
            document = body.getDocument();
            parameters = body.getParameters();
        } catch (MalformedMimetypeException e) {
            exchange.sendResponseHeaders(422, 0);
            exchange.getResponseBody().write("Malformed MIME Type".getBytes());
            exchange.getResponseBody().close();
            return;
            
        } catch (Exception e) {
            exchange.sendResponseHeaders(422, 0);
            exchange.getResponseBody().write(e.getMessage().getBytes());
            exchange.getResponseBody().close();
            System.out.println(e.getMessage());
            return;
        }
        
        var responder = new ServerResponder(exchange);
        var job = new SigningJob(document, parameters, responder);
        
        autogram.showSigningDialog(job);
    }
}
