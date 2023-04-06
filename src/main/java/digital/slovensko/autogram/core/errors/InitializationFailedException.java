package digital.slovensko.autogram.core.errors;

public class InitializationFailedException extends AutogramException {
    public InitializationFailedException() {
        super("Nastala chyba", "Nepodarilo sa prečítať kartu", "Pravdepodobne nemáte zapojenú čítačku alebo vloženú kartu do čítačky.\n\nSkontrolujte, či máte všetko správne zapojené a skúste znova.");
    }
}
