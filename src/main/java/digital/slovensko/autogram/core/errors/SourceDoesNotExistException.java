package digital.slovensko.autogram.core.errors;

public class SourceDoesNotExistException extends AutogramException {
    public SourceDoesNotExistException() {
        super("Nastala chyba", "Zdrojový súbor / adresár neexistuje", "Zadali ste zdrojový súbor / adresár, ktorý neexistuje");
    }

    public SourceDoesNotExistException(String sourcePath) {
        super("Nastala chyba", "Zdrojový súbor / adresár neexistuje", "Zadali ste zdrojový súbor / adresár \"" + sourcePath + "\", ktorý neexistuje");
    }
}
