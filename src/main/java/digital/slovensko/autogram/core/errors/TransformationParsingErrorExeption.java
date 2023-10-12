package digital.slovensko.autogram.core.errors;

public class TransformationParsingErrorExeption extends AutogramException {
    public TransformationParsingErrorExeption(String message) {
        super("Nastala chyba", "Nastala chyba pri čítaní XSLT transformácie", message);
    }
}
