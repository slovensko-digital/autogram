package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import static com.octosign.whitelabel.ui.Utils.*;

/**
 * Generic document exchanged during communication
 */
public class Document implements Cloneable {
    protected String id;
    protected String title;
    protected String content;
    protected String legalEffect;
    protected MimeType mimeType;

    public Document() {}

    public Document(String id, String title, String content, String legalEffect) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.legalEffect = legalEffect;
    }

    public Document(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
        setMimeType(document.getMimeType());
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getLegalEffect() { return legalEffect; }

    public void setLegalEffect(String legalEffect) { this.legalEffect = legalEffect; }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

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
        document.setMimeType(mimeType);

        if (mimeType.equalsTypeSubtype(MimeType.XML)) {
            return buildXMLDocument(document, parameters, mimeType);
        } else if(mimeType.equalsTypeSubtype(MimeType.PDF)) {
            return new PDFDocument(document);
        } else {
            throw new IntegrationException(Code.UNSUPPORTED_FORMAT, "Document format not supported: " + mimeType);
        }
    }

    private static XMLDocument buildXMLDocument(Document document, SignatureParameters parameters, MimeType mimeType) {
        var schema = parameters.getSchema();
        var transformation = parameters.getTransformation();

        if (mimeType.isBase64()) {
            try {
                document.setContent(decodeBase64(document.getContent()));
                schema = decodeBase64(schema);
                transformation = decodeBase64(transformation);
            } catch (Exception e) {
                throw new IntegrationException(Code.DECODING_FAILED, e);
            }

            // TODO don't forget about this too
            mimeType.removeParameter("base64");
        }
        return new XMLDocument(document, schema, transformation);
    }
}
