package digital.slovensko.autogram.core;

public abstract class Responder {
    abstract public void onDocumentSigned(SignedDocument signedDocument);

    abstract public void onDocumentSignFailed(SigningJob job, SigningError error);
}
