package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;

public class ServerResponder extends Responder {
    private final HttpExchange exchange;

    public ServerResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onDocumentSigned(DSSDocument r) {
        try {
            // TODO return signed document
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        try {
            exchange.sendResponseHeaders(500, 0);
            exchange.getResponseBody().close();;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
