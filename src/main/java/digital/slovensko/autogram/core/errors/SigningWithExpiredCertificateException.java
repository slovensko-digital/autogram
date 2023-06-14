package digital.slovensko.autogram.core.errors;

public class SigningWithExpiredCertificateException extends AutogramException {
    public SigningWithExpiredCertificateException() {
        super("Nastala chyba", "Platnosť podpisového certifikátu vypršala", "Certifkát už nie je platný.\n\nPravdepodobne budete musieť získať nový a platný certifikát.");
    }
}
