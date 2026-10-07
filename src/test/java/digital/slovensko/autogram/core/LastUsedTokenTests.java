package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.PKCS11TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.drivers.TokenSlot;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LastUsedTokenTests {
    private static final PKCS11TokenDriver EID = driver("eid");
    private static final PKCS11TokenDriver GEMALTO = driver("gemalto");
    private static final PKCS11TokenDriver MONET = driver("monet");
    private static final PKCS11TokenDriver KEYSTORE = driver("keystore");

    private static final TokenSlot EID_ACS = new TokenSlot(1, 0, "Sig_ZEP", "Idemia", "Cosmo", "ffffffff", "ACS ACR38U-CCID; Sig_ZEP");
    private static final TokenSlot EID_OMNIKEY = new TokenSlot(2, 1, "Sig_ZEP", "Idemia", "Cosmo", "ffffffff", "HID Global OMNIKEY 5422 Smartcard Reader; Sig_ZEP");
    private static final TokenSlot GEMALTO_1 = new TokenSlot(0, 0, "Card #6E1E37488C911948", "Gemalto", "IDPrime", "6E1E37488C911948", "ACS ACR38U-CCID");
    private static final TokenSlot GEMALTO_2 = new TokenSlot(1, 1, "Card #6E1E37488C911948 (Digital", "Gemalto", "IDPrime", "6E1E37488C911948", "ACS ACR38U-CCID (Digital Signature Pin)");

    private static final TokenOptions OPTIONS = new TokenOptions(List.of(
            new TokenOption(EID, EID_ACS),
            new TokenOption(EID, EID_OMNIKEY),
            new TokenOption(GEMALTO, GEMALTO_1),
            new TokenOption(GEMALTO, GEMALTO_2),
            new TokenOption(KEYSTORE, null)), List.of(MONET), List.of(), false);

    @Test
    void testSameCardInSameReaderIsFound() {
        var last = LastUsedToken.of(new TokenOption(EID, EID_OMNIKEY));

        assertEquals(Optional.of(new TokenOption(EID, EID_OMNIKEY)), last.findIn(OPTIONS));
    }

    @Test
    void testSlotOfCardIsFound() {
        var last = LastUsedToken.of(new TokenOption(GEMALTO, GEMALTO_2));

        assertEquals(Optional.of(new TokenOption(GEMALTO, GEMALTO_2)), last.findIn(OPTIONS));
    }

    @Test
    void testCardMovedToAnotherReaderIsFound() {
        var last = new LastUsedToken("gemalto", GEMALTO_2.label(), GEMALTO_2.serialNumber(), "Some Other Reader (Digital Signature Pin)");

        assertEquals(Optional.of(new TokenOption(GEMALTO, GEMALTO_2)), last.findIn(OPTIONS));
    }

    @Test
    void testDriverUsedWithoutCardIsFound() {
        assertEquals(Optional.of(new TokenOption(KEYSTORE, null)), LastUsedToken.of(new TokenOption(KEYSTORE, null)).findIn(OPTIONS));
    }

    @Test
    void testDriverUsedWithoutCardIsFoundAmongOtherDrivers() {
        assertEquals(Optional.of(new TokenOption(MONET, null)), LastUsedToken.of(new TokenOption(MONET, null)).findIn(OPTIONS));
    }

    @Test
    void testFormerDefaultDriverPicksItsFirstCard() {
        assertEquals(Optional.of(new TokenOption(EID, EID_ACS)), new LastUsedToken("eid", null, null, null).findIn(OPTIONS));
    }

    @Test
    void testDriverIsFoundWhenCardsCouldNotBeSearched() {
        var allDrivers = new TokenOptions(List.of(new TokenOption(EID, null), new TokenOption(GEMALTO, null)), List.of(), List.of(), false);
        var last = LastUsedToken.of(new TokenOption(GEMALTO, GEMALTO_2));

        assertEquals(Optional.of(new TokenOption(GEMALTO, null)), last.findIn(allDrivers));
    }

    @Test
    void testCardNotInsertedIsNotFound() {
        var withoutGemaltoCard = new TokenOptions(List.of(new TokenOption(EID, EID_ACS)), List.of(), List.of(GEMALTO), false);

        assertEquals(Optional.empty(), LastUsedToken.of(new TokenOption(GEMALTO, GEMALTO_2)).findIn(withoutGemaltoCard));
    }

    @Test
    void testOtherCardOfSameDriverIsNotFound() {
        var last = new LastUsedToken("gemalto", "Card #0000000000000000", "0000000000000000", "ACS ACR38U-CCID");

        assertEquals(Optional.empty(), last.findIn(OPTIONS));
    }

    @Test
    void testDriverNotInstalledIsNotFound() {
        assertEquals(Optional.empty(), new LastUsedToken("cz_eid", null, null, null).findIn(OPTIONS));
        assertEquals(Optional.empty(), new LastUsedToken("cz_eid", "Card", "1234", "Reader").findIn(OPTIONS));
    }

    @Test
    void testInvalidValuesAreNotFound() {
        assertTrue(new LastUsedToken(null, null, null, null).isEmpty());
        assertEquals(Optional.empty(), new LastUsedToken(null, null, null, null).findIn(OPTIONS));
        assertEquals(Optional.empty(), new LastUsedToken("  ", "Sig_ZEP", null, null).findIn(OPTIONS));
        assertEquals(Optional.empty(), new LastUsedToken("%$#garbage", "\u0000", "x", "y").findIn(OPTIONS));
    }

    @Test
    void testUserSettingsIgnoreEmptyLastUsedToken() {
        var settings = new UserSettings();
        assertEquals(Optional.empty(), settings.getLastUsedToken());

        settings.setLastUsedToken(new LastUsedToken("", null, null, null));
        assertEquals(Optional.empty(), settings.getLastUsedToken());

        settings.setLastUsedToken(LastUsedToken.of(new TokenOption(KEYSTORE, null)));
        assertEquals(Optional.of(new LastUsedToken("keystore", "", "", "")), settings.getLastUsedToken());
    }

    private static PKCS11TokenDriver driver(String shortname) {
        return new PKCS11TokenDriver(shortname, Path.of(""), shortname, "");
    }
}
