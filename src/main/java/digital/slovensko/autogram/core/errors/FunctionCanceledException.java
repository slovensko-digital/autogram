package digital.slovensko.autogram.core.errors;

public class FunctionCanceledException extends AutogramException {
    public FunctionCanceledException() {
        super("Nastala chyba", "Nezadali ste bezpečnostný kód", "Pravdepodobne ste len zavreli okno na zadanie bezpečnostného kódu. Skúste znova.");
    }
}
