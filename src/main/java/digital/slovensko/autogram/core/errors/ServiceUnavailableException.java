package digital.slovensko.autogram.core.errors;

public class ServiceUnavailableException extends AutogramException {
    public ServiceUnavailableException(String url) {
        super("Chyba pripojenia", "Spojenie so serverom zlyhalo", "Nepodarilo sa nadviazať spojenie so serverom na adrese: " + url + ".");
    }

    public ServiceUnavailableException(String url, Throwable e) {
        super("Chyba pripojenia", "Spojenie so serverom zlyhalo", "Nepodarilo sa nadviazať spojenie so serverom na adrese: " + url + ".", e);
    }
}
