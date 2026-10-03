package digital.slovensko.autogram.drivers;

import java.util.List;
import java.util.Optional;

/**
 * What the user can choose from when picking where the signing certificate is.
 *
 * @param found tokens (cards) found by all drivers, plus drivers that don't list tokens (e.g. keystore file)
 * @param otherDrivers drivers that didn't find any token, the user may still want one if it couldn't detect the card
 */
public record TokenOptions(List<TokenOption> found, List<TokenDriver> otherDrivers) {
    public boolean isEmpty() {
        return found.isEmpty() && otherDrivers.isEmpty();
    }

    public boolean hasTokens() {
        return found.stream().anyMatch(TokenOption::isToken);
    }

    /**
     * Returns the option to use without asking: the only token (card) found, or the only option there is.
     */
    public Optional<TokenOption> getAutomaticOption() {
        var tokens = found.stream().filter(TokenOption::isToken).toList();
        if (tokens.size() == 1)
            return Optional.of(tokens.get(0));

        if (!tokens.isEmpty())
            return Optional.empty();

        if (found.size() == 1 && otherDrivers.isEmpty())
            return Optional.of(found.get(0));

        if (found.isEmpty() && otherDrivers.size() == 1)
            return Optional.of(new TokenOption(otherDrivers.get(0), null));

        return Optional.empty();
    }

    /**
     * Options of the given driver only, empty if the driver isn't among the options.
     */
    public Optional<TokenOptions> forDriver(String driverShortname) {
        var filteredFound = found.stream().filter(o -> o.driver().getShortname().equals(driverShortname)).toList();
        var filteredOthers = otherDrivers.stream().filter(d -> d.getShortname().equals(driverShortname)).toList();
        var filtered = new TokenOptions(filteredFound, filteredOthers);

        return filtered.isEmpty() ? Optional.empty() : Optional.of(filtered);
    }
}
