package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenSlot;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User friendly description of a token option, with only as much detail as needed to tell the options apart.
 *
 * @param driverName name of the driver, e.g. "Občiansky preukaz"
 * @param readerName card reader, only if the driver found cards in more readers, otherwise null
 * @param slotNumber 1-based number of the slot on the card, only if the card has more slots, otherwise null
 * @param qualified instead of slot number for eID card slots: true for the qualified certificate, false for the other
 */
public record TokenOptionDescription(String driverName, String readerName, Integer slotNumber, Boolean qualified) {
    /**
     * Describes the options, the result is in the same order.
     */
    public static List<TokenOptionDescription> describeAll(List<TokenOption> options) {
        var tokens = options.stream().filter(TokenOption::isToken).toList();
        var cards = tokens.stream().collect(Collectors.groupingBy(Card::of, LinkedHashMap::new, Collectors.toList()));

        return options.stream().map((option) -> {
            if (!option.isToken())
                return new TokenOptionDescription(option.driver().getName(), null, null, null);

            var card = Card.of(option);
            var cardSlots = cards.get(card);
            var readerName = getCardReaderName(cardSlots);
            var driverCardCount = cards.keySet().stream().filter((c) -> c.driver() == card.driver()).count();

            Integer slotNumber = null;
            Boolean qualified = null;
            if (cardSlots.size() > 1) {
                qualified = isEidQualifiedSlot(option.slot());
                if (qualified == null)
                    slotNumber = cardSlots.indexOf(option) + 1;
            }

            return new TokenOptionDescription(option.driver().getName(), driverCardCount > 1 ? readerName : null,
                    slotNumber, qualified);
        }).toList();
    }

    /**
     * A physical card, slots of the same card share the serial number, or the reader if the serial number is unknown.
     */
    private record Card(TokenDriver driver, String id) {
        static Card of(TokenOption option) {
            var slot = option.slot();
            if (isRealSerialNumber(slot.serialNumber()))
                return new Card(option.driver(), "serial:" + slot.serialNumber());

            return new Card(option.driver(), "reader:" + getReaderName(slot));
        }
    }

    // slots of one card may describe the reader differently, e.g. "Reader" and "Reader (Digital Signature Pin)"
    private static String getCardReaderName(List<TokenOption> cardSlots) {
        return cardSlots.stream()
                .map((option) -> getReaderName(option.slot()))
                .min(Comparator.comparingInt(String::length))
                .orElse("");
    }

    // eID driver describes slots as "<reader>; <token label>"
    private static String getReaderName(TokenSlot slot) {
        var readerName = slot.readerName();
        if (!slot.label().isEmpty() && readerName.endsWith("; " + slot.label()))
            return readerName.substring(0, readerName.length() - slot.label().length() - 2);

        return readerName;
    }

    // eID card has a slot with the qualified certificate (Sig_ZEP) and one with the non-qualified (Sig_EP)
    private static Boolean isEidQualifiedSlot(TokenSlot slot) {
        if (slot.label().equalsIgnoreCase("Sig_ZEP"))
            return true;

        if (slot.label().equalsIgnoreCase("Sig_EP"))
            return false;

        return null;
    }

    // some drivers report placeholders like "ffffffff" instead of the serial number
    private static boolean isRealSerialNumber(String serialNumber) {
        return !serialNumber.isEmpty() && serialNumber.chars().distinct().count() > 1;
    }
}
