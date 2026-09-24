package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

/**
 * The single output port for signing. Adapters (GUI, CLI, HTTP) override only the
 * events they care about; every event is a no-op by default.
 *
 * <p>The domain counts every document into the active {@link Batch} exactly once
 * before calling the matching {@code onDocument*} method, so implementations must
 * never mutate batch counters themselves.
 */
public interface SigningResponder {
    /** The batch was started and the adapter may start submitting documents. */
    default void onBatchStarted(Batch batch, SigningMode mode) {
    }

    /** The batch could not be started (cancelled, conflict, ...). */
    default void onBatchStartFailed(AutogramException error) {
    }

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
