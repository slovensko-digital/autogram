package digital.slovensko.autogram.core.errors;

/**
 * Refused while a batch is in progress, e.g. reading certificates - another connection to the card would log it out
 * once closed, and the batch couldn't sign anymore.
 */
public class BatchInProgressException extends AutogramException {
    public BatchInProgressException() {
        super();
    }
}
