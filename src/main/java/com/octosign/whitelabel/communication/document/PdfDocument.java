package com.octosign.whitelabel.communication.document;

/**
 * PDF document for signing
 */
public class PdfDocument extends Document {

    public static final String MIME_TYPE = "application/pdf";

    public PdfDocument() { }

    public PdfDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
    }

}
