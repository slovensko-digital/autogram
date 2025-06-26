package digital.slovensko.autogram.core.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class PasswordNotProvidedException extends AutogramException {
    public PasswordNotProvidedException() {
        super("Nastala chyba", "Nezadali ste podpisový PIN alebo heslo", "Pravdepodobne ste len zavreli okno na zadanie podpisového PINu alebo hesla. Skúste znova.");
    }
}
