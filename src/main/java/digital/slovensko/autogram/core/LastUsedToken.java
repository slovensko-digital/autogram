package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The token (card) or driver the user last signed with, to preselect it next time. Stored in user preferences, so any
 * value may be missing, outdated or invalid - it only ever helps to preselect an option that is offered.
 *
 * @param driverShortname driver used, e.g. "eid"
 * @param label token label, empty if the driver itself was used (e.g. keystore file)
 * @param serialNumber token serial number, empty if the driver itself was used
 * @param readerName slot description (usually the reader), empty if the driver itself was used
 */
public record LastUsedToken(String driverShortname, String label, String serialNumber, String readerName) {
    public LastUsedToken {
        driverShortname = driverShortname == null ? "" : driverShortname.trim();
        label = label == null ? "" : label;
        serialNumber = serialNumber == null ? "" : serialNumber;
        readerName = readerName == null ? "" : readerName;
    }

    public static LastUsedToken of(TokenOption option) {
        if (!option.isToken())
            return new LastUsedToken(option.driver().getShortname(), null, null, null);

        var slot = option.slot();
        return new LastUsedToken(option.driver().getShortname(), slot.label(), slot.serialNumber(), slot.readerName());
    }

    public boolean isEmpty() {
        return driverShortname.isEmpty();
    }

    public boolean isToken() {
        return !label.isEmpty() || !serialNumber.isEmpty() || !readerName.isEmpty();
    }

    /**
     * The offered option matching this one: the same card in the same reader, or in another reader, or the same
     * driver if it was used without a card (or the card can't be searched for right now). Empty if there's no such
     * option, e.g. the driver isn't installed anymore or the card isn't inserted.
     */
    public Optional<TokenOption> findIn(TokenOptions options) {
        if (isEmpty())
            return Optional.empty();

        var tokens = options.found().stream().filter(TokenOption::isToken).filter(this::isSameDriver).toList();
        if (isToken()) {
            var sameCard = tokens.stream().filter(this::isSameCard);
            var sameCardInSameReader = tokens.stream().filter(this::isSameCard).filter((o) -> o.slot().readerName().equals(readerName));
            var found = sameCardInSameReader.findFirst().or(sameCard::findFirst);
            if (found.isPresent())
                return found;
        }

        var driverOption = options.found().stream().filter((o) -> !o.isToken()).filter(this::isSameDriver).findFirst();
        if (driverOption.isPresent() || isToken())
            return driverOption;

        // the driver was used without a card, e.g. its card can't be detected - it may be among other drivers
        return Stream.concat(tokens.stream(), options.otherDrivers().stream()
                        .filter((driver) -> driver.getShortname().equals(driverShortname))
                        .map((driver) -> new TokenOption(driver, null)))
                .findFirst();
    }

    private boolean isSameDriver(TokenOption option) {
        return option.driver().getShortname().equals(driverShortname);
    }

    private boolean isSameCard(TokenOption option) {
        return option.slot().label().equals(label) && option.slot().serialNumber().equals(serialNumber);
    }
}
