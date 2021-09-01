package com.octosign.whitelabel.communication.server.endpoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.XMLDocument;
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
        if (onSign == null) {
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
            var signedDocument = onSign.apply(document).get();
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
        var document = signRequest.getDocument();
        var parameters = signRequest.getParameters();
        var mimeType = MimeType.parse(signRequest.getPayloadMimeType());

        return switch (mimeType.getType()) {
            case XMLDocument.MIME_TYPE -> {
                var schema = parameters.getSchema();
                var transformation = parameters.getTransformation();

                if (mimeType.isBase64()) {
                    document.setContent(decode(document.getContent()));
                    schema = decode(schema);
                    transformation = decode(transformation);
                }

                yield new XMLDocument(document, schema, transformation);
            }
            default -> throw new IllegalArgumentException("Unsupported MIME type");
        };
    }

    private String decode(String input) {
        if (input == null || input.isBlank()) return null;

        var decoder = Base64.getDecoder();
        return new String(decoder.decode(input));
    }
}
