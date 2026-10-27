package digital.slovensko.autogram.core.errors;

/** The signing certificate is not of a type the document requires, e.g. a batch key picked before the document came. */
public class SigningCertificateNotAllowedException extends AutogramException {
    public SigningCertificateNotAllowedException() {
        super();
    }
}
