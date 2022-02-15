package com.octosign.whitelabel.communication.document;
import com.octosign.whitelabel.communication.SignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.io.Files.*;
import static com.octosign.whitelabel.communication.MimeType.*;
import static com.octosign.whitelabel.ui.ConfigurationProperties.*;

/**
 * Generic document exchanged during communication
 */
public class Document {
    public static final Path DEFAULT_DOWNLOAD_DIR = Path.of(System.getProperty("java.io.tmpdir"), getProperty("app.shortName"), "documents").toAbsolutePath();

    public static final Set<String> ALLOWED_TYPES = new HashSet<>(
            List.of("pdf", "doc", "docx", "odt", "txt", "xml", "rtf", "png", "gif", "tif", "tiff", "bmp", "jpg", "jpeg", "xml", "pdf", "xsd", "xls")
    );

    protected String id;
    protected String filename;
    protected byte[] content;
    transient File downloadedFile;

    public Document() {}

    public Document(String id, String filename, byte[] content) {
        this.id = id;
        this.filename = filename;
        this.content = content;
        this.downloadedFile = null;
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

    public boolean isOfAllowedType() { return ALLOWED_TYPES.contains(getFileExtension(filename)); }

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
        } else if (document.isOfAllowedType()) {
            return new BinaryDocument(document);
        } else {
            return new ForbiddenTypeDocument(document);
        }
    }

    public File asDownloadedFile() {
        if (downloadedFile == null || !downloadedFile.exists()) {
            downloadedFile = downloadFile(DEFAULT_DOWNLOAD_DIR);
        }

        return downloadedFile;
    }

    public File downloadFile(Path directory) {
        File target = getFileLocation(directory);
        directory.toFile().mkdirs();

        try (var stream = new FileOutputStream(target)) {
            stream.write(getContent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to save file!");
        }

        return target;
    }

    private File getFileLocation(Path directory) {
        int counter = 0;
        File result;

        while ((result = directory.resolve(buildFilename(counter)).toFile()).exists())
            counter++;

        return result;
    }

    private String buildFilename(int counter) {
        String basename = getNameWithoutExtension(filename);
        String extension = getFileExtension(filename);

        if (counter == 0)
            return String.format("%s.%s", basename, extension);
        else
            return String.format("%s_%d.%s", basename, counter, extension);
    }
}
