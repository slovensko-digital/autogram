package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.*;
import com.octosign.whitelabel.communication.CommunicationError.Code;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.ui.IntegrationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.octosign.whitelabel.ui.Main.getProperty;

public class SignEndpoint extends WriteEndpoint<SignRequest, Document> {

    private Function<SignatureUnit, Future<Document>> onSign;

    public SignEndpoint(Server server, int initialNonce) {
        super(server, initialNonce);
    }

    public void setOnSign(Function<SignatureUnit, Future<Document>> onSign) {
        this.onSign = onSign;
    }

    @Override
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response) throws IntegrationException {
        if (onSign == null) {
            var error = new CommunicationError(Code.NOT_READY, getProperty("error.serverNotReady"));
            var errorResponse = new Response<CommunicationError>(request.getExchange()).asError(HttpURLConnection.HTTP_CONFLICT, error);

            try {
                errorResponse.send();
            } catch (IOException ex) {
                throw new IntegrationException(String.format("Unable to send error response: %s", errorResponse.getBody()));
            }
            return null;
        }

        var signRequest = request.getBody();

        Document document = null;
        try {
            document = getSpecificDocument(signRequest);
        } catch (IntegrationException e) {
            e.printStackTrace();
        }

        var template = extractTemplateFrom(request);
        var parameters = resolveParameters(signRequest, template);

        var signatureUnit = new SignatureUnit(document, parameters);

        try {
            var signedDocument = onSign.apply(signatureUnit).get();
            return response.setBody(signedDocument);
        } catch (Exception e) {
            // TODO: We should do a better job with the error response here:
            // We can differentiate between application errors (500), user errors (502), missing certificate/UI closed (503)
            var error = new CommunicationError(Code.SIGNING_FAILED, getProperty("error.signingFailed"), e.getMessage());
            var errorResponse = new Response<CommunicationError>(request.getExchange()).asError(HttpURLConnection.HTTP_INTERNAL_ERROR, error);

            try {
                errorResponse.send();
            } catch (IOException ex) {
                throw new IntegrationException(String.format("Unable to send error response: %s", errorResponse.getBody()));
            }
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

    /**
     * Creates and prepares payload type specific document
     *
     * TODO: Consider extracting this out as this shouldn't be specific to server mode
     *
     * @param signRequest object representing particular signing request data and params
     * @return Specific document like XMLDocument type-widened to Document
     */
    private static Document getSpecificDocument(SignRequest signRequest) throws IntegrationException {
        var document = signRequest.getDocument();
        var parameters = signRequest.getParameters();
        var mimeType = MimeType.parse(signRequest.getPayloadMimeType());

        return switch (mimeType.getType()) {
            case XMLDocument.MIME_TYPE -> buildXMLDocument(document, parameters, mimeType);
            case PDFDocument.MIME_TYPE -> new PDFDocument(document);
            default -> throw new IllegalArgumentException(String.format("Unsupported MIME type: %s", mimeType.getType()));
        };
    }

    private static XMLDocument buildXMLDocument(Document document, SignatureParameters parameters, MimeType mimeType) throws IntegrationException {
        var schema = parameters.getSchema();
        var transformation = parameters.getTransformation();

        if (mimeType.isBase64()) {
            try {
                document.setContent(decode(document.getContent()));
                schema = decode(schema);
                transformation = decode(transformation);
            } catch (Exception e) {
                throw new IntegrationException(getProperty("error.decodingFailed", e.getMessage()));
            }
        }

        return new XMLDocument(document, schema, transformation);
    }

    private static String decode(String input) throws IntegrationException {
        if (input == null || input.isBlank()) return null;

        var decoder = Base64.getDecoder();
        return new String(decoder.decode(input));
    }

    private static Configuration extractTemplateFrom(Request<?> request) {
        var templateId = request.getQueryParams().get("template");
        if (templateId == null || templateId.isEmpty()) return null;

        var templateName = LOWER_HYPHEN.to(UPPER_UNDERSCORE, templateId);
        return Configuration.from(templateName);
    }

    private static SignatureParameters resolveParameters(SignRequest signRequest, Configuration template) {
        var sourceParams = (template != null) ? template.parameters() : signRequest.getParameters();

        return new SignatureParameters.Builder(sourceParams)
                .schema(sourceParams.getSchema())
                .transformation(sourceParams.getTransformation())
                .signaturePolicyId(sourceParams.getSignaturePolicyId())
                .signaturePolicyContent(sourceParams.getSignaturePolicyContent())
                .build();
    }
}
