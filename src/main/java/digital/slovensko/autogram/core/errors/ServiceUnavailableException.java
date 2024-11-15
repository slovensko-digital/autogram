package digital.slovensko.autogram.core.errors;

public class ServiceUnavailableException extends AutogramException {
    public ServiceUnavailableException(String url) {
        this(url, null);
    }

    public ServiceUnavailableException(String url, Throwable e) {
        super("Chyba pripojenia", "Spojenie so serverom zlyhalo", "Nepodarilo sa nadviazať spojenie so serverom na adrese: " + url + "\n\nSkúste podpisovanie opakovať neskôr. Ak problém pretrváva dlhodobo, ozvite sa nám, prosím, emailom na podpora@slovensko.digital", e);
    }
}
