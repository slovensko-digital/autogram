package digital.slovensko.autogram.drivers;

/**
 * A token (card) present in one of the slots (readers) of a token driver.
 *
 * @param slotId PKCS#11 slot ID, stable while the driver is loaded
 * @param slotListIndex index of the slot in the list of all slots reported by the driver, used if slotId doesn't fit
 *                      into int (SunPKCS11 limitation)
 * @param label token label, e.g. "SIG_EP"
 * @param manufacturer token manufacturer
 * @param model token model
 * @param serialNumber token serial number
 * @param readerName slot description, usually the name of the card reader
 */
public record TokenSlot(long slotId, int slotListIndex, String label, String manufacturer, String model, String serialNumber, String readerName) {
    public String getName() {
        if (!label.isEmpty())
            return label;

        if (!model.isEmpty())
            return model;

        return manufacturer;
    }
}
