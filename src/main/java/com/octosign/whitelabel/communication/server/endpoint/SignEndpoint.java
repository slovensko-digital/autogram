package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.XmlDocument;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;

public class SignEndpoint extends WriteEndpoint<SignRequest, Document> {

    private Function<Document, CompletableFuture<Document>> onSign;

    public SignEndpoint(Server server, int initialNonce) {
        super(server, initialNonce);
    }

    public void setOnSign(Function<Document, CompletableFuture<Document>> onSign) {
        this.onSign = onSign;
    }

    @Override
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response) throws IOException {
        if (this.onSign == null) {
            var error = new CommunicationError(Code.NOT_READY, "Server is not ready yet");
            new Response<CommunicationError>(request.getExchange())
            .asError(HttpURLConnection.HTTP_CONFLICT, error)
            .send();
            return null;
        }

        var signRequest = request.getBody();
        var parameters = signRequest.getParameters();
        var document = getSpecificDocument(signRequest);

        try {
            // TODO: Add using of SignatureParameters
            var signedDocument = this.onSign.apply(document).get();
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

    /**
     * Creates and prepares payload type specific document
     *
     * TODO: Consider extracting this out as this shouldn't be specific to server mode
     *
     * @param signRequest
     * @return Specific document like XMLDocument type-widened to Document
     */
    private Document getSpecificDocument(SignRequest signRequest) {
        var mimeType = signRequest.getPayloadMimeType();
        var document = signRequest.getDocument();
        var parameters = signRequest.getParameters();

        // MIME type can have optional params separated by ;, e.g. some/type;base64
        var mimeTypeParts = mimeType.split(";");
        var baseMimeType = mimeTypeParts[0];
        var isBase64 = mimeTypeParts.length > 1 && mimeTypeParts[1].equals("base64");

        Document specificDocument = null;
        switch (baseMimeType) {
            case XmlDocument.MIME_TYPE:
                var xmlDocument = new XmlDocument(document);
                if (isBase64) {
                    var decoder = Base64.getDecoder();
                    var binaryContent = decoder.decode(xmlDocument.getContent());
                    xmlDocument.setContent(new String(binaryContent));
                    if (parameters.getTransformation() != null) {
                        var binaryTransformation = decoder.decode(parameters.getTransformation());
                        xmlDocument.setTransformation(new String(binaryTransformation));
                    }
                } else {
                    if (parameters.getTransformation() != null) {
                        xmlDocument.setTransformation(parameters.getTransformation());
                    }
                }

                specificDocument = xmlDocument;
                break;
        }

        return specificDocument;
    }

}
