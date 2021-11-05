package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.CommunicationError;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;
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
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response) throws IOException {
        if (onSign == null) {
            var error = new CommunicationError(Code.NOT_READY, "Server is not ready yet");
            new Response<CommunicationError>(request.getExchange())
                .asError(HttpURLConnection.HTTP_CONFLICT, error)
                .send();
            return null;
        }

        var signRequest = request.getBody();
        var document = getSpecificDocument(signRequest);
        var signatureUnit = new SignatureUnit(document, signRequest.getParameters());

        try {
            var signedDocument = onSign.apply(signatureUnit).get();
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
    protected String[] getAllowedMethods() { return new String[]{ "POST" }; }

    /**
     * Creates and prepares payload type specific document
     *
     * TODO: Consider extracting this out as this shouldn't be specific to server mode
     *
     * @param signRequest object representing particular signing request data and params
     * @return Specific document like XMLDocument type-widened to Document
     */
    private Document getSpecificDocument(SignRequest signRequest) {
        var document = signRequest.getDocument();
        var parameters = signRequest.getParameters();
        var mimeType = MimeType.parse(signRequest.getPayloadMimeType());

        if (mimeType.equalsTypeSubtype(MimeType.XML)) {
            var schema = parameters.getSchema();
            var transformation = parameters.getTransformation();

            if (mimeType.isBase64()) {
                document.setContent(decode(document.getContent()));
                schema = decode(schema);
                transformation = decode(transformation);
            }

            return new XMLDocument(document, schema, transformation);
        } else if(mimeType.equalsTypeSubtype(MimeType.PDF)) {
            return new PDFDocument(document);
        } else {
            throw new IllegalArgumentException("Unsupported MIME type");
        }
    }

    private String decode(String input) {
        if (input == null || input.isBlank()) return null;

        var decoder = Base64.getDecoder();
        return new String(decoder.decode(input));
    }
}
