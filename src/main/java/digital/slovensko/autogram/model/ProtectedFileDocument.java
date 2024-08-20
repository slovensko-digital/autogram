package digital.slovensko.autogram.model;

import eu.europa.esig.dss.model.FileDocument;

import java.io.File;

public class ProtectedFileDocument extends FileDocument implements ProtectedDSSDocument {
    private char[] password = new char[0];

    public ProtectedFileDocument(String path) {
        super(path);
    }

    public ProtectedFileDocument(File file) {
        super(file);
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
