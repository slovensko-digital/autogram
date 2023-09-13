package digital.slovensko.autogram.core.errors;

public class SlotIdIsNotANumberException extends AutogramException {
    public SlotIdIsNotANumberException() {
        super("Nastala chyba", "Zadaný slot ID nie je číslo", "Zadali ste slot ID, ktorý nie je číslo");
    }

    public SlotIdIsNotANumberException(String slotId) {
        super("Nastala chyba", "Zadaný slot ID nie je číslo", "Zadali ste slot ID \"" + slotId + "\", ktorý nie je číslo");
    }
}
