package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;

public class SignEndpoint extends WriteEndpoint<SignRequest, Document> {

    private Function<Document, CompletableFuture<Document>> onSign;

    public void setOnSign(Function<Document, CompletableFuture<Document>> onSign) {
        this.onSign = onSign;
    }

    @Override
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response) throws IOException {
        if (onSign == null) {
            var error = new CommunicationError(Code.NOT_READY, "Server is not ready yet");
            new Response<CommunicationError>(request.getExchange())
                .asError(HttpURLConnection.HTTP_CONFLICT, error)
                .send();
            return null;
        }

        // TODO: Add using of SignatureParameters
        var signRequest = request.getBody();

        try {
            var signedDocument = onSign.apply(signRequest.getDocument()).get();
            return response.setBody(signedDocument);
        } catch (Exception e) {
            // TODO: We should do a better job with the error response here:
            // We can differentiate between application errors (500), user errors (502), missing certificate/UI closed (503)

            var error = new CommunicationError(Code.SIGNING_FAILED, "Signing failed.", e.getMessage());
            new Response<CommunicationError>(request.getExchange())
                .asError(HttpURLConnection.HTTP_INTERNAL_ERROR, error)
                .send();
            return null;
        }
    }

    @Override
    protected Class<SignRequest> getRequestClass() {
        return SignRequest.class;
    }

    @Override
    protected Class<Document> getResponseClass() {
        return Document.class;
    }

    @Override
    protected String[] getAllowedMethods() {
        return new String[]{ "POST" };
    }

}
