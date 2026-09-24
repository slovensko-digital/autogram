package digital.slovensko.autogram.core;

/**
 * How the documents of a batch are signed. It is only a parameter of the
 * {@code startBatch} command; {@link Batch} does not remember it.
 */
public enum SigningMode {
    /** One signing key (and PIN) for the whole batch; documents are signed in a loop. */
    AUTOMATED,
    /** Each document goes through the interactive signing flow. */
    INTERACTIVE
}
