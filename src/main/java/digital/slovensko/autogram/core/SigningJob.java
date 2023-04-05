package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;

    public SigningJob(CommonDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
    }

    public DSSDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    public void onDocumentSignFailed(SigningJob job, SigningError e) {
        responder.onDocumentSignFailed(job, e);
    }

    public void onDocumentSigned(DSSDocument signedDocument) {
        responder.onDocumentSigned(signedDocument);
    }

    public boolean isPlainText() {
        return false; // TODO
    }

    public boolean isHTML() {
        return false; // TODO
    }

    public boolean isPDF() {
        return true; // TODO
    }

    public String getDocumentAsPlainText() {
        try {
            return new String(document.openStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDocumentAsHTML() {
        try {
            return new String(document.openStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDocumentAsBase64Encoded() {
        try {
            return new String(Base64.getEncoder().encode(document.openStream().readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
