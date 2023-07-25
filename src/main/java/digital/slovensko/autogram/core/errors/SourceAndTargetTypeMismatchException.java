package digital.slovensko.autogram.core.errors;

public class SourceAndTargetTypeMismatchException extends AutogramException {
    public SourceAndTargetTypeMismatchException() {
        super("Nastala chyba", "Zdrojové a cieľové umiestnenia sú rôzneho typu (súbor / adresár)", "Zadali ste zdrojové a cieľové umiestnenia, ktoré sú rôzneho typu (súbor / adresár)");
    }

}
