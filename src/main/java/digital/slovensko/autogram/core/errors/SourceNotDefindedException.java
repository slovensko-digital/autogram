package digital.slovensko.autogram.core.errors;

public class SourceNotDefindedException extends AutogramException {
    public SourceNotDefindedException() {
        super("Nastala chyba", "Zdrojový súbor / adresár nebol definovaný", "Nezadali ste zdrojový súbor / adresár na podpis");
    }

}
