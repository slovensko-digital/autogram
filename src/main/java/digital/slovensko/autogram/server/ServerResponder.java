package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.SignResponse;

import java.io.IOException;
import java.util.Base64;

public class ServerResponder extends Responder {
    private final HttpExchange exchange;

    public ServerResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onDocumentSigned(SignedDocument signedDocument) throws AutogramException {
        var signer = signedDocument.getCertificate().getSubject().getPrincipal().toString();
        var issuer = signedDocument.getCertificate().getIssuer().getPrincipal().toString();

        try {
            var b64document = Base64.getEncoder().encodeToString(signedDocument.getDocument().openStream().readAllBytes());
            EndpointUtils.respondWith(new SignResponse(b64document, signer, issuer), exchange);
        } catch (IOException e) {
            throw new ResponseNetworkErrorException(e);
        }
    }

    @Override
    public void onDocumentSignFailed(AutogramException error) {
        EndpointUtils.respondWithError(ErrorResponse.buildFromException(error), exchange);
    }
}
