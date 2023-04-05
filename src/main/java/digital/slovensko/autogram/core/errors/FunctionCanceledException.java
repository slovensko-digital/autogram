package digital.slovensko.autogram.core.errors;

public class FunctionCanceledException extends AutogramException {
    public FunctionCanceledException() {
        super("Nastala chyba", "Nezadali ste PIN", "Pravdepodobne ste len zavreli okno na zadanie PINu. Skúste znova.");
    }
}
