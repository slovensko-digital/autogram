package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.SigningResponder;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.server.dto.SignResponse;

import java.io.IOException;
import java.util.Base64;

public class ServerResponder implements SigningResponder {
    private final HttpExchange exchange;

    public ServerResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onDocumentSigned(SignedDocument signedDocument) {
        var signer = signedDocument.getCertificate().getSubject().getPrincipal().toString();
        var issuer = signedDocument.getCertificate().getIssuer().getPrincipal().toString();
        var document = signedDocument.getDocument();

        try {
            var b64document = Base64.getEncoder().encodeToString(document.openStream().readAllBytes());
            var mimeType = document.getMimeType() != null ? document.getMimeType().getMimeTypeString() : "application/octet-stream";
            var filename = document.getName() != null ? document.getName() : "signed-document";

            EndpointUtils.respondWith(new SignResponse(b64document, mimeType, filename, signer, issuer), exchange);
        } catch (IOException e) {
            throw new ResponseNetworkErrorException(e);
        }
    }

    @Override
    public void onDocumentFailed(AutogramException error) {
        EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(error), exchange);
    }

    @Override
    public void onDocumentCanceled() {
        respondCanceled();
    }

    @Override
    public void onDocumentSkipped() {
        respondCanceled();
    }

    @Override
    public void onDocumentSkippedRemaining() {
        respondCanceled();
    }

    private void respondCanceled() {
        EndpointUtils.respondWithError(
                ErrorResponseBuilder.buildFromException(new SigningCanceledByUserException()), exchange);
    }
}
