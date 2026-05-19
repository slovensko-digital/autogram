package digital.slovensko.autogram.core.errors;

public class TokenDriverDoesNotExistException extends AutogramException {

    public TokenDriverDoesNotExistException(String tokenDriver) {
        super(new Object[]{tokenDriver});
    }
}
