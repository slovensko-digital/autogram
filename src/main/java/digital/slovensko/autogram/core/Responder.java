package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

public abstract class Responder {
    abstract public void onDocumentSigned(SignedDocument signedDocument);

    abstract public void onDocumentSignFailed(SigningJob job, AutogramException error);
}
