package digital.slovensko.autogram.core;

public interface SignatureTokenSettings {
    boolean getForceContextSpecificLoginEnabled();
    int getDriverSlotIndex(String tokenDriverShortname);
}
