package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.TokenCodes.Problem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenCodesTests {
    @ParameterizedTest
    @CsvSource(nullValues = "-", value = {
            "123456, -",
            "12345, WRONG_LENGTH",
            "1234567, WRONG_LENGTH",
            "12345a, NOT_DIGITS",
            // letters are reported before the length, the user typed them by mistake
            "12a, NOT_DIGITS",
            // digits of other scripts are digits for Character.isDigit, but not for the card
            "١٢٣٤٥٦, NOT_DIGITS",
            "123 456, NOT_DIGITS",
    })
    void testEidCodeHasSixDigits(String code, Problem problem) {
        assertEquals(Optional.ofNullable(problem), TokenCodes.EID.check(code.toCharArray()));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "-", value = {
            "1234, -",
            "12345678, -",
            "12a4, NOT_DIGITS",
    })
    void testDigitsOnlyCodeHasAnyLength(String code, Problem problem) {
        assertEquals(Optional.ofNullable(problem), TokenCodes.DIGITS.check(code.toCharArray()));
    }

    @ParameterizedTest
    @CsvSource({"abc!", "12a4", "1"})
    void testAnyCodeIsAccepted(String code) {
        assertEquals(Optional.empty(), TokenCodes.ANY.check(code.toCharArray()));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "-", value = {
            "1a2 3, 123, true",
            "123, 123, false",
            "ľščť, '', true",
    })
    void testDigitsOnlyCodeKeepsOnlyDigits(String typed, String kept, boolean removed) {
        var result = TokenCodes.DIGITS.keepAllowed(typed);
        assertEquals(kept, result.kept());
        assertEquals(removed, result.removedSomething());
    }

    @ParameterizedTest
    @CsvSource({"1a2 3", "heslo!"})
    void testAnyCodeKeepsEverything(String typed) {
        var result = TokenCodes.ANY.keepAllowed(typed);
        assertEquals(typed, result.kept());
        assertEquals(false, result.removedSomething());
    }

    @ParameterizedTest
    @CsvSource(nullValues = "-", value = {
            // some tokens need no code, the user leaves the field empty
            "ANY, true, -",
            "DIGITS, true, -",
            "ANY, false, EMPTY",
            // eID always needs one, an empty BOK would count as a wrong one
            "EID, true, EMPTY",
            "EID, false, EMPTY",
    })
    void testEmptyCode(String rules, boolean emptyAllowed, Problem problem) {
        var codes = switch (rules) {
            case "EID" -> TokenCodes.EID;
            case "DIGITS" -> TokenCodes.DIGITS;
            default -> TokenCodes.ANY;
        };

        assertEquals(Optional.ofNullable(problem), codes.check(new char[0], emptyAllowed));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "-", value = {"123456, -", "12a456, NOT_DIGITS", "1234, WRONG_LENGTH"})
    void testNonEmptyCodeIsCheckedRegardlessOfEmptyAllowed(String code, Problem problem) {
        assertEquals(Optional.ofNullable(problem), TokenCodes.EID.check(code.toCharArray(), true));
    }
}
