package digital.slovensko.autogram.core.errors;

public class SlotIndexIsNotANumberException extends AutogramException {

    public SlotIndexIsNotANumberException(String slotId) {
        super(new Object[]{slotId});
    }
}
