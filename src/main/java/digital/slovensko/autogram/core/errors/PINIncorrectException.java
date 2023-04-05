package digital.slovensko.autogram.core.errors;

public class PINIncorrectException extends AutogramException {
    public PINIncorrectException() {
        super("Nastala chyba", "Zadali ste nesprávny PIN", "Pravdepodobne ste spravili chybu pri zadávaní PINu.\n\nUistite sa, že zadávate správny PIN a skúste znova.");
    }
}
