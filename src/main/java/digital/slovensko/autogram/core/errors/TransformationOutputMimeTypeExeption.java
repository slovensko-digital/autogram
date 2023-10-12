package digital.slovensko.autogram.core.errors;

public class TransformationOutputMimeTypeExeption extends AutogramException {
    public TransformationOutputMimeTypeExeption(String message) {
        super("Nastala chyba", "Nastala chyba pri čítaní XSLT transformácie", message);
    }
}
