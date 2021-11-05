package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.UserException;

import java.util.concurrent.Future;
import java.util.function.Function;

import static com.octosign.whitelabel.ui.I18n.translate;

public class SignEndpoint extends WriteEndpoint<SignRequest, Document> {

    private Function<SignatureUnit, Future<Document>> onSign;

    public SignEndpoint(Server server, int initialNonce) {
        super(server, initialNonce);
    }

    public void setOnSign(Function<SignatureUnit, Future<Document>> onSign) { this.onSign = onSign; }

    @Override
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response) {
        if (onSign == null)
            throw new UserException(Code.NOT_READY, translate("error.serverNotReady"));

        var signRequest = request.getBody();
        var document = Document.getSpecificDocument(signRequest);
        var parameters = signRequest.getParameters();
        var signatureUnit = new SignatureUnit(document, parameters);

        try {
            var signedDocument = onSign.apply(signatureUnit).get();

            return response.setBody(signedDocument);
        } catch (Exception e) {
            // TODO: We should do a better job with the error response here:
            // We can differentiate between application errors (500), user errors (502), missing certificate/UI closed (503)
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
    protected String[] getAllowedMethods() { return new String[] { "POST" }; }
}
