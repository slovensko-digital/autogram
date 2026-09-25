package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;

/**
 * Receives document signing outcomes. The domain updates batch counters before
 * notifying responders, so implementations must not update them themselves.
 */
public interface SigningResponder {
    default void onDocumentSigned(SignedDocument signedDocument) {
    }

    default void onDocumentFailed(AutogramException error) {
    }

    default void onDocumentCanceled() {
    }

    default void onDocumentSkipped() {
    }

    default void onDocumentSkippedRemaining() {
    }
}
