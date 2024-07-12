package digital.slovensko.autogram.core.errors;

public class ServiceUnavailableException extends AutogramException {
    public ServiceUnavailableException(String url) {
        super("Spojenie so serverom zlyhalo", "Nepodarilo sa spojiť so serverom", "Nepodarilo sa kontaktovať " + url + ".");
    }
}
