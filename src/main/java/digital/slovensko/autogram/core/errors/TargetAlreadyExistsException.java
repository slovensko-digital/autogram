package digital.slovensko.autogram.core.errors;

public class TargetAlreadyExistsException extends AutogramException {
    public TargetAlreadyExistsException() {
        super("Nastala chyba", "Cieľový súbor / adresár už existuje", "Zadali ste cieľový súbor / adresár, ktorý už existuje");
    }
}
