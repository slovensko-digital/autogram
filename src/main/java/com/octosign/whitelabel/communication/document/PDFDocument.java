package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.communication.MimeType;

/**
 * PDF document for signing
 */
public class PDFDocument extends Document {

    public static final MimeType mimeType = MimeType.PDF;

    public PDFDocument() { }

    public PDFDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
    }

}
