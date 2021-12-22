package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.*;
import com.octosign.whitelabel.error_handling.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class SignEndpoint extends WriteEndpoint<SignRequest, Document> {

    private Function<SignatureUnit, Future<Document>> onSign;

    public SignEndpoint(Server server, int initialNonce) {
        super(server, initialNonce);
    }

    public void setOnSign(Function<SignatureUnit, Future<Document>> onSign) {
        this.onSign = onSign;
    }

    @Override
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response)
            throws Throwable {
        if (onSign == null)
            throw new IntegrationException(Code.NOT_READY);

        var signRequest = request.getBody();
        // TODO: Decode from base64 only here

        var document = Document.getSpecificDocument(signRequest);
        var parameters = signRequest.getParameters();
        var signatureUnit = new SignatureUnit(document, parameters);

        Document signedDocument;
        try {
            signedDocument = onSign.apply(signatureUnit).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw e.getCause();
        }

        // TODO: Encode back to base64 only here

        return response.setBody(signedDocument);
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
