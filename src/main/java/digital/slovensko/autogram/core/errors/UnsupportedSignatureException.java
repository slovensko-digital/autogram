package digital.slovensko.autogram.core.errors;

public class UnsupportedSignatureException extends AutogramException {
    // TODO define the message
    public UnsupportedSignatureException(String signatureType) {
        super("Nastala chyba", "", "");
    }
}
