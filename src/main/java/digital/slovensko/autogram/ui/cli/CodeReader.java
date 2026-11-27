package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.TokenCodes;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Reads a code (PIN, BOK, password) from the terminal until it follows the token's rules. The terminal can't drop
 * letters as they're typed, so a code breaking the rules is rejected after it's entered and never reaches the token.
 */
class CodeReader {
    /**
     * @param input reads the hidden code, returns null at the end of input
     * @param name  what the code is called in the messages, e.g. "BOK"
     * @return the code, null if the user canceled
     */
    static char[] read(Supplier<char[]> input, Consumer<String> errors, TokenCodes codes, String name) {
        while (true) {
            var code = input.get();
            if (code == null)
                return null;

            // some tokens need no code
            var problem = codes.check(code, true);
            if (problem.isEmpty())
                return code;

            Arrays.fill(code, '\0');
            errors.accept(switch (problem.get()) {
                case EMPTY -> "Enter " + name;
                case NOT_DIGITS -> name + " must only include numbers 0 to 9";
                case WRONG_LENGTH -> name + " must be " + codes.length() + " digits";
            });
        }
    }
}
