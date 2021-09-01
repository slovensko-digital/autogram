package com.octosign.whitelabel.communication.document;

/**
 * PDF document for signing
 */
public class PDFDocument extends Document {

    public static final String MIME_TYPE = "application/pdf";

    public PDFDocument() { }

    public PDFDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
    }

}
