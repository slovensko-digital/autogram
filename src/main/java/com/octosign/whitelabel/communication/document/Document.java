package com.octosign.whitelabel.communication.document;
import com.octosign.whitelabel.communication.SignRequest;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.octosign.whitelabel.communication.MimeType.*;

/**
 * Generic document exchanged during communication
 */
public class Document {
    public static final Set<String> ALLOWED_TYPES = new HashSet<>(
            List.of("pdf", "doc", "docx", "odt", "txt", "xml", "rtf", "png", "gif", "tif", "tiff", "bmp", "jpg", "jpeg", "xml", "pdf", "xsd", "xls")
    );

    protected String id;
    protected String filename;
    protected byte[] content;

    public Document() {}

    public Document(String id, String filename, byte[] content) {
        this.id = id;
        this.filename = filename;
        this.content = content;
    }

    public Document(Document d) {
        this(d.id, d.filename, d.content);
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }

    public byte[] getContent() { return content; }

    public void setContent(byte[] content) { this.content = content; }

    public String getContentString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    public boolean isPermitted() {
        return ALLOWED_TYPES.contains(extension());
    }

    public String basename() {
        return filename.substring(0, filename.lastIndexOf("."));
    }

    public String extension() {
        return filename.substring(filename.lastIndexOf(".") + 1);
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
        } else if (document.isPermitted()) {
            return new BinaryDocument(document);
        } else {
            return new ForbiddenTypeDocument(document);
        }
    }
}
