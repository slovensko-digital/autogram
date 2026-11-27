package digital.slovensko.autogram.core;

import java.util.Optional;

/**
 * Codes the user enters for a token - the code to log in (keystore password, BOK for eID) and the signing PIN - and
 * which characters they may have. Codes with other characters are never sent to the token: a card counts every wrong
 * code and blocks itself after a few, e.g. when the user types the letters of the Slovak keyboard's top row instead of
 * digits without seeing them.
 *
 * @param bok         the login code is the BOK of the Slovak eID card, and Autogram asks for both codes itself even
 *                    though the token reports a protected authentication path (eID klient's own windows don't work
 *                    from the command line nor in the Mac App Store sandbox)
 * @param digitsOnly  the codes have only digits 0-9
 * @param length      exact number of digits, 0 if any
 */
public record TokenCodes(boolean bok, boolean digitsOnly, int length) {
    public static final TokenCodes ANY = new TokenCodes(false, false, 0);
    public static final TokenCodes DIGITS = new TokenCodes(false, true, 0);
    public static final TokenCodes EID = new TokenCodes(true, true, 6);

    public enum Problem {EMPTY, NOT_DIGITS, WRONG_LENGTH}

    public record Kept(String kept, boolean removedSomething) {
    }

    /**
     * @param emptyAllowed the token may need no code, so the user leaves the field empty; never for an exact length
     * @return what's wrong with the code the user entered, empty if it can be sent to the token
     */
    public Optional<Problem> check(char[] code, boolean emptyAllowed) {
        if (code.length == 0)
            return emptyAllowed && length == 0 ? Optional.empty() : Optional.of(Problem.EMPTY);

        return check(code);
    }

    /**
     * @return what's wrong with a non-empty code, empty if the code can be sent to the token
     */
    public Optional<Problem> check(char[] code) {
        if (!digitsOnly)
            return Optional.empty();

        for (char c : code)
            if (!isDigit(c))
                return Optional.of(Problem.NOT_DIGITS);

        if (length > 0 && code.length != length)
            return Optional.of(Problem.WRONG_LENGTH);

        return Optional.empty();
    }

    /**
     * Drops the characters the code can't have, as the user types or pastes them.
     */
    public Kept keepAllowed(String typed) {
        if (!digitsOnly)
            return new Kept(typed, false);

        var kept = new StringBuilder();
        for (char c : typed.toCharArray())
            if (isDigit(c))
                kept.append(c);

        return new Kept(kept.toString(), kept.length() != typed.length());
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
