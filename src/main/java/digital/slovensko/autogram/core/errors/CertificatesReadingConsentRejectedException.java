package digital.slovensko.autogram.core.errors;

public class CertificatesReadingConsentRejectedException extends AutogramException {
    public CertificatesReadingConsentRejectedException() {
        super("Načítavanie certifikátov zlyhalo", "Zamietnuté", "Používateľ zamietol žiadosť o prečítanie podpisových certifikátov");
    }
}
