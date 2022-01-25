package com.octosign.whitelabel.communication.document;
import com.octosign.whitelabel.communication.SignRequest;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.nio.charset.StandardCharsets;

import static com.octosign.whitelabel.communication.MimeType.*;

/**
 * Generic document exchanged during communication
 */
public class Document {
    protected String id;
    protected String title;
    protected byte[] content;
    protected String legalEffect;

    public Document() {}

    public Document(String id, String title, byte[] content, String legalEffect) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.legalEffect = legalEffect;
    }

    public Document(Document d) {
        this(d.id, d.title, d.content, d.legalEffect);
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public byte[] getContent() { return content; }

    public void setContent(byte[] content) { this.content = content; }

    public String getLegalEffect() { return legalEffect; }

    public void setLegalEffect(String legalEffect) { this.legalEffect = legalEffect; }

    public String getContentString() {
        return new String(content, StandardCharsets.UTF_8);
    }
    /**
     * Creates and prepares payload type specific document
     *
     * @param signRequest object representing particular signing request data and params
     * @return Specific document like XMLDocument type-widened to Document
     */
    public static Document getSpecificDocument(SignRequest signRequest) {
        var document = signRequest.getDocument();
        var mimeType = signRequest.getPayloadMimeType();

        if (mimeType.is(XML)) {
            return new XMLDocument(document);
        } else if (mimeType.is(PDF)) {
            return new PDFDocument(document);
        } else if (BinaryDocument.isNotBlacklisted(mimeType)) {
            return new BinaryDocument(document);
        } else {
            throw new IntegrationException(Code.UNSUPPORTED_FORMAT, "Document format not supported: " + mimeType);
        }
    }
}
