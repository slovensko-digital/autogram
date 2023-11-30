package digital.slovensko.autogram.core.errors;

public class SlotIndexIsNotANumberException extends AutogramException {
    public SlotIndexIsNotANumberException() {
        super("Nastala chyba", "Zadaný slot ID nie je číslo", "Zadali ste slot ID, ktorý nie je číslo");
    }

    public SlotIndexIsNotANumberException(String slotId) {
        super("Nastala chyba", "Zadaný slot ID nie je číslo", "Zadali ste slot ID \"" + slotId + "\", ktorý nie je číslo");
    }
}
