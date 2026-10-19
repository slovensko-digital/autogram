package digital.slovensko.autogram.core.errors;

/** A PDF protected with a password can't be read, so it can't be shown nor signed. */
public class PdfPasswordProtectedException extends AutogramException {
    public PdfPasswordProtectedException() {
        super("LOCKED_PDF");
    }
}
