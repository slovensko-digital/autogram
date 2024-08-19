package digital.slovensko.autogram.model;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.io.InputStream;

public class ProtectedInMemoryDocument extends InMemoryDocument implements ProtectedDSSDocument {
    private char[] password;

    public ProtectedInMemoryDocument(byte[] bytes) {
        super(bytes);
    }

    public ProtectedInMemoryDocument(byte[] bytes, String name) {
        super(bytes, name);
    }

    public ProtectedInMemoryDocument(byte[] bytes, String name, MimeType mimeType) {
        super(bytes, name, mimeType);
    }

    public ProtectedInMemoryDocument(InputStream inputStream) {
        super(inputStream);
    }

    public ProtectedInMemoryDocument(InputStream inputStream, String name) {
        super(inputStream, name);
    }

    public ProtectedInMemoryDocument(InputStream inputStream, String name, MimeType mimeType) {
        super(inputStream, name, mimeType);
    }

    @Override
    public char[] getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(char[] password) {
        this.password = password;
    }
}
