package digital.slovensko.autogram.core.errors;

/** The document is too large to be signed with the memory available to Autogram. */
public class NotEnoughMemoryException extends AutogramException {
    public NotEnoughMemoryException(OutOfMemoryError e) {
        super(e);
    }
}
