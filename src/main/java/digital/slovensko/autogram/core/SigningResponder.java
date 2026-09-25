package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;

/** Receives document signing outcomes. Implementations must not update batch counters. */
public interface SigningResponder {
    void onDocumentSigned(SignedDocument signedDocument);

    void onDocumentFailed(AutogramException error);

    void onDocumentCanceled();

    void onDocumentSkipped();

    void onDocumentSkippedRemaining();
}
