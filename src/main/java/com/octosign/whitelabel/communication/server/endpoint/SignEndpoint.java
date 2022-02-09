package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.SignedData;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.*;
import com.octosign.whitelabel.error_handling.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class SignEndpoint extends WriteEndpoint<SignRequest, SignedData> {

    private Function<SignatureUnit, Future<SignedData>> onSign;

    public SignEndpoint(Server server, int initialNonce) {
        super(server, initialNonce);
    }

    public void setOnSign(Function<SignatureUnit, Future<SignedData>> onSign) {
        this.onSign = onSign;
    }

    @Override
    protected Response<SignedData> handleRequest(Request<SignRequest> request, Response<SignedData> response) throws Throwable {
        if (onSign == null)
            throw new IntegrationException(Code.NOT_READY);

        var signRequest = request.getBody();

        var document = Document.getSpecificDocument(signRequest);
        var parameters = signRequest.getParameters();
        var mimeType = signRequest.getPayloadMimeType();

        var signatureUnit = new SignatureUnit(document, parameters, mimeType);

        SignedData signedData;
        try {
            signedData = onSign.apply(signatureUnit).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw e.getCause();
        }

        return response.setBody(signedData);
    }

    @Override
    protected Class<SignRequest> getRequestClass() {
        return SignRequest.class;
    }

    @Override
    protected Class<SignedData> getResponseClass() {
        return SignedData.class;
    }

    @Override
    protected String[] getAllowedMethods() {
        return new String[]{ "POST" };
    }

}
