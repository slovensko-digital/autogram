package digital.slovensko.autogram.core.errors;

public class PINIncorrectException extends AutogramException {
    public PINIncorrectException() {
        super("Nastala chyba", "Zadali ste nesprávny bezpečnostný kód", "Pravdepodobne ste spravili chybu pri zadávaní bezpečnostného kódu.\n\nUistite sa, že zadávate správny bezpečnostný kód a skúste znova.");
    }
}
