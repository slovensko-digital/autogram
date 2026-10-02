package digital.slovensko.autogram.core;

public enum SigningMode {
    /** One signing key (and PIN) for the whole batch; documents are signed in a loop. */
    BULK,
    /** Each document goes through the interactive signing flow. */
    INTERACTIVE
}
