package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.server.dto.ErrorResponseBody;
import digital.slovensko.autogram.server.dto.SignResponse;
import digital.slovensko.autogram.util.ServerErrorResponderUtils;

import java.io.IOException;
import java.util.Base64;

public class ServerResponder extends Responder {
    private final HttpExchange exchange;
    private final static Gson gson = new Gson();

    public ServerResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onDocumentSigned(SignedDocument signedDocument) {
        var gson = new Gson();
        var signer = signedDocument.getCertificate().getSubject().getPrincipal().toString();
        var issuer = signedDocument.getCertificate().getIssuer().getPrincipal().toString();
        String b64document;

        try {
            b64document = Base64.getEncoder().encodeToString(signedDocument.getDocument().openStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var response = new SignResponse(b64document, signer, issuer);

        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDocumentSignFailed(SigningJob job, AutogramException error) {
        var code = ServerErrorResponderUtils.getErrorCodeFromException(error);
        var response = new ErrorResponseBody(code, error.getSubheading(), error.getDescription());

        var statusCode = ServerErrorResponderUtils.getStatusCodeFromException(error);

        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, 0);
            if (statusCode != 204)
                exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
