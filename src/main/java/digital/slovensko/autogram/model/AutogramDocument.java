package digital.slovensko.autogram.model;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AutogramDocument implements DSSDocument {
    private final DSSDocument document;

    private char[] openDocumentPassword = new char[0];
    private char[] masterPassword = new char[0];

    public AutogramDocument(DSSDocument document) {
        this.document = document;
    }

    public boolean hasOpenDocumentPassword() {
        return openDocumentPassword.length > 0;
    }

    public char[] getOpenDocumentPassword() {
        return openDocumentPassword;
    }

    public void setOpenDocumentPassword(char[] openDocumentPassword) {
        this.openDocumentPassword = openDocumentPassword;
    }

    public char[] getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(char[] masterPassword) {
        this.masterPassword = masterPassword;
    }

    public char[] getSigningPassword() {
        return hasOpenDocumentPassword() ? openDocumentPassword : masterPassword;
    }

    public String getName() {
        return document.getName();
    }

    @Override
    public void setName(String s) {

    }

    public MimeType getMimeType() {
        return document.getMimeType();
    }

    @Override
    public void setMimeType(MimeType mimeType) {
        document.setMimeType(mimeType);
    }

    @Override
    public void save(String s) throws IOException {
        document.save(s);
    }

    @Override
    public String getDigest(DigestAlgorithm digestAlgorithm) {
        return document.getDigest(digestAlgorithm);
    }

    public InputStream openStream() {
        return document.openStream();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        document.writeTo(outputStream);
    }

    public DSSDocument getDSSDocument() {
        return document;
    }
}
