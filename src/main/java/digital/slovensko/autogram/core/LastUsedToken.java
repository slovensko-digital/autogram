package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;

import java.util.Optional;

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

        if (isToken()) {
            var sameCard = findSameCard(options);
            if (sameCard != null)
                return Optional.of(sameCard);
        }

        for (var option : options.found()) {
            if (!option.isToken() && isSameDriver(option))
                return Optional.of(option);
        }

        if (isToken())
            return Optional.empty();

        // the driver was used without a card, e.g. its card can't be detected - it may have found one now, or it may be
        // among other drivers
        for (var option : options.found()) {
            if (isSameDriver(option))
                return Optional.of(option);
        }

        for (var driver : options.otherDrivers()) {
            if (driver.getShortname().equals(driverShortname))
                return Optional.of(new TokenOption(driver, null));
        }

        return Optional.empty();
    }

    /**
     * The same card in the same reader, or in another reader. Null if it isn't inserted.
     */
    private TokenOption findSameCard(TokenOptions options) {
        TokenOption inOtherReader = null;
        for (var option : options.found()) {
            if (!option.isToken() || !isSameDriver(option) || !isSameCard(option))
                continue;

            if (option.slot().readerName().equals(readerName))
                return option;

            if (inOtherReader == null)
                inOtherReader = option;
        }

        return inOtherReader;
    }

    private boolean isSameDriver(TokenOption option) {
        return option.driver().getShortname().equals(driverShortname);
    }

    private boolean isSameCard(TokenOption option) {
        return option.slot().label().equals(label) && option.slot().serialNumber().equals(serialNumber);
    }
}
