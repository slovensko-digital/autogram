package digital.slovensko.autogram.core.errors;

public class TokenDriverDoesNotExistException extends AutogramException {
    public TokenDriverDoesNotExistException() {
        super("Nastala chyba", "Token driver neexistuje", "Zadali ste token driver, ktorý neexistuje");
    }

    public TokenDriverDoesNotExistException(String tokenDriver) {
        super("Nastala chyba", "Token driver neexistuje", "Zadali ste token driver \"" + tokenDriver + "\", ktorý neexistuje");
    }
}
