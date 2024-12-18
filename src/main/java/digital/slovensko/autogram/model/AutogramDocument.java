package digital.slovensko.autogram.model;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.InputStream;

public class AutogramDocument {
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

    public MimeType getMimeType() {
        return document.getMimeType();
    }

    public InputStream openStream() {
        return document.openStream();
    }

    public DSSDocument getDSSDocument() {
        return document;
    }
}
