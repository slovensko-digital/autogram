package digital.slovensko.autogram.core;

public class Settings implements PasswordManagerSettings, SignatureTokenSettings {
    private int slotId = -1;

    @Override
    public boolean cacheContextSpecificPasswordEnabled() {
        return true; // TODO
    }

    @Override
    public boolean forceContextSpecificLoginEnabled() {
        return true; // TODO
    }


    public int getSlotId() {
        return slotId;
    }

    @Override
    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }
}
