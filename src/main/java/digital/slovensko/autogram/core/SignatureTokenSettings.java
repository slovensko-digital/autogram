package digital.slovensko.autogram.core;

public interface SignatureTokenSettings {
    boolean forceContextSpecificLoginEnabled();
    int getSlotId();
    void setSlotId(int slotId);
}
