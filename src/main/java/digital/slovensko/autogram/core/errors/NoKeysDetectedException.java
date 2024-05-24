package digital.slovensko.autogram.core.errors;

import digital.slovensko.autogram.drivers.TokenDriver;

public class NoKeysDetectedException extends AutogramException {
    public NoKeysDetectedException(String helperText) {
        super("Nastala chyba", "Nenašli sa žiadne podpisové certifikáty", "V úložisku certifikátov sa pravdepodobne nenachádzajú žiadne platné podpisové certifikáty, ktoré by sa dali použiť na podpisovanie. " + helperText, null);
    }
}
