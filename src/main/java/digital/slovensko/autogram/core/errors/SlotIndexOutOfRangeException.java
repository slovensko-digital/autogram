package digital.slovensko.autogram.core.errors;

import eu.europa.esig.dss.model.DSSException;

public class SlotIndexOutOfRangeException extends AutogramException {
    public SlotIndexOutOfRangeException(DSSException e) {
        super("Nesprávny slot index", "Slot index je vyšší než počet slotov na karte", "Nastavený slot index presahuje počet slotov karty. Zmeňte nastavenie a skúste to znova.", e);
    }
}
