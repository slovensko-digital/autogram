package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.TokenCodes;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeReaderTests {
    @Test
    void testCodeBreakingTheRulesIsAskedForAgain() {
        var typed = new ArrayDeque<>(List.of("12ľ456", "12345", "", "123456"));
        var errors = new ArrayList<String>();

        var code = CodeReader.read(() -> typed.remove().toCharArray(), errors::add, TokenCodes.EID, "BOK");

        assertArrayEquals("123456".toCharArray(), code);
        assertEquals(3, errors.size(), errors.toString());
        assertTrue(errors.get(0).contains("0 to 9"), errors.get(0));
        assertTrue(errors.get(1).contains("6 digits"), errors.get(1));
        assertTrue(errors.get(2).contains("Enter BOK"), errors.get(2));
    }

    @Test
    void testCanceledCodeIsNotAskedForAgain() {
        var errors = new ArrayList<String>();

        assertNull(CodeReader.read(() -> null, errors::add, TokenCodes.EID, "BOK"));
        assertEquals(List.of(), errors);
    }

    @Test
    void testEmptyCodeIsAcceptedIfTheRulesAllowIt() {
        // some tokens need no code
        var errors = new ArrayList<String>();

        assertArrayEquals(new char[0], CodeReader.read(() -> new char[0], errors::add, TokenCodes.DIGITS, "Keystore password"));
        assertEquals(List.of(), errors);
    }

    @Test
    void testAnyCodeIsAccepted() {
        var errors = new ArrayList<String>();

        assertArrayEquals("heslo!".toCharArray(), CodeReader.read("heslo!"::toCharArray, errors::add, TokenCodes.ANY, "Keystore password"));
        assertEquals(List.of(), errors);
    }
}
