package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.MalformedMimetypeException;

import static com.octosign.whitelabel.ui.Utils.*;

/**
 * Generic document exchanged during communication
 */
public class Document implements Cloneable {
    protected String id;
    protected String title;
    protected String content;
    protected String legalEffect;

    public Document() {}

    public Document(String id, String title, String content, String legalEffect) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.legalEffect = legalEffect;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getLegalEffect() { return legalEffect; }

    public void setLegalEffect(String legalEffect) { this.legalEffect = legalEffect; }

    @Override
    public Document clone() {
        return new Document(id, title, content, legalEffect);
    }

    /**
     * Creates and prepares payload type specific document
     *
     * @param signRequest object representing particular signing request data and params
     * @return Specific document like XMLDocument type-widened to Document
     */
    public static Document getSpecificDocument(SignRequest signRequest) {
        var document = signRequest.getDocument();
        var parameters = signRequest.getParameters();

        // TODO: Assert document, parameters, and payloadMimeType are not null

        MimeType mimeType;
        try {
            mimeType = MimeType.parse(signRequest.getPayloadMimeType());
        } catch (Exception e) {
            throw new IntegrationException(Code.MALFORMED_MIMETYPE, e);
        }

        if (mimeType.equalsTypeSubtype(MimeType.XML)) {
            return document.buildXMLDocument(parameters, mimeType);
        } else if(mimeType.equalsTypeSubtype(MimeType.PDF)) {
            return new PDFDocument(document);
        } else {
            throw new IntegrationException(Code.UNSUPPORTED_FORMAT, "Document format not supported: " + mimeType);
        }
    }

    private XMLDocument buildXMLDocument(SignatureParameters parameters, MimeType mimeType) {
        var schema = parameters.getSchema();
        var transformation = parameters.getTransformation();

        if (mimeType.isBase64()) {
            try {
                setContent(decodeBase64(getContent()));
                schema = decodeBase64(schema);
                transformation = decodeBase64(transformation);
            } catch (Exception e) {
                throw new IntegrationException(Code.DECODING_FAILED, e);
            }
        }
        return new XMLDocument(this, schema, transformation);
    }
}
