package digital.slovensko.autogram.core.errors;

public class SigningWithExpiredCertificateException extends AutogramException {
    public SigningWithExpiredCertificateException() {
        super("Nastala chyba", "Platnosť podpisového certifikátu vypršala", "Certifkát v čase podpisu nie je platný.\n\nProsím zmente podpisový certifkát.");
    }
}
