package digital.slovensko.autogram.drivers;

/**
 * Something the user can pick to sign with: a token (card) found by a driver, or the driver itself.
 *
 * @param driver driver to use
 * @param slot token to use, null to use the driver's default slot
 */
public record TokenOption(TokenDriver driver, TokenSlot slot) {
    public boolean isToken() {
        return slot != null;
    }
}
