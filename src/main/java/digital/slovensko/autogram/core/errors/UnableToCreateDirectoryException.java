package digital.slovensko.autogram.core.errors;

public class UnableToCreateDirectoryException extends AutogramException {
    public UnableToCreateDirectoryException() {
        super("Nastala chyba", "Nepodarilo sa vytvoriť adresár", "Nepodarilo sa vytvoriť adresár");
    }

}
