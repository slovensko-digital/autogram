package digital.slovensko.autogram.core.errors;

public class TargetDirectoryDoesNotExistException extends AutogramException {
    public TargetDirectoryDoesNotExistException() {
        super("Nastala chyba", "Cieľový adresár neexistuje", "Zadali ste cieľový adresár, ktorý neexistuje");
    }
}
