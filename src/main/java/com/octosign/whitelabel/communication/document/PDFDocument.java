package com.octosign.whitelabel.communication.document;

/**
 * PDF document for signing
 */
public class PDFDocument extends Document {
    public PDFDocument() { }

    public PDFDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
        setMimeType(document.getMimeType());
    }

}
