package digital.slovensko.autogram.drivers;

import java.util.List;
import java.util.Optional;

/**
 * What the user can choose from when picking where the signing certificate is.
 *
 * @param found tokens (cards) found by all drivers, plus drivers that don't list tokens (e.g. keystore file)
 * @param otherDrivers drivers that can't tell whether they have a token: not searched (e.g. eObčanka), or the search
 *                     failed - offered every time, never used without asking
 * @param unavailableDrivers drivers that can't be used now: they found no token, or didn't respond in time
 * @param notResponding true if card readers or some driver didn't respond in time, so their cards are unknown
 */
public record TokenOptions(List<TokenOption> found, List<TokenDriver> otherDrivers, List<TokenDriver> unavailableDrivers,
                           boolean notResponding) {
    /**
     * True if there are no drivers at all.
     */
    public boolean isEmpty() {
        return found.isEmpty() && otherDrivers.isEmpty() && unavailableDrivers.isEmpty();
    }

    public boolean hasTokens() {
        return found.stream().anyMatch(TokenOption::isToken);
    }

    /**
     * True if drivers searched for tokens (cards) and found none. Not if some driver can't tell, its card may be there.
     */
    public boolean noTokenFound() {
        return !hasTokens() && otherDrivers.isEmpty() && !unavailableDrivers.isEmpty();
    }

    /**
     * Returns the option to use without asking: the only token (card) found, or the only option there is. A driver
     * that found no card is never used, it would only fail to read the card.
     */
    public Optional<TokenOption> getAutomaticOption() {
        // the user may want a card of a driver that can't tell whether it's there, e.g. eObčanka
        if (!otherDrivers.isEmpty())
            return Optional.empty();

        var tokens = found.stream().filter(TokenOption::isToken).toList();
        if (tokens.size() == 1)
            return Optional.of(tokens.get(0));

        // the user should rather learn their card wasn't found than sign with something else
        if (!tokens.isEmpty() || !unavailableDrivers.isEmpty())
            return Optional.empty();

        if (found.size() == 1)
            return Optional.of(found.get(0));

        return Optional.empty();
    }

    /**
     * Options of the given driver only, empty if the driver isn't among the options.
     */
    public Optional<TokenOptions> forDriver(String driverShortname) {
        var filteredFound = found.stream().filter(o -> o.driver().getShortname().equals(driverShortname)).toList();
        var filteredOthers = otherDrivers.stream().filter(d -> d.getShortname().equals(driverShortname)).toList();
        var filteredUnavailable = unavailableDrivers.stream().filter(d -> d.getShortname().equals(driverShortname)).toList();
        var filtered = new TokenOptions(filteredFound, filteredOthers, filteredUnavailable, notResponding);

        return filtered.isEmpty() ? Optional.empty() : Optional.of(filtered);
    }
}
