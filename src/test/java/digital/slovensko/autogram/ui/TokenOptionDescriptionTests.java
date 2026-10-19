package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.drivers.PKCS11TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenSlot;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenOptionDescriptionTests {
    private static final PKCS11TokenDriver EID = driver("Občiansky preukaz");
    private static final PKCS11TokenDriver GEMALTO = driver("Gemalto IDPrime 940");
    private static final PKCS11TokenDriver KEYSTORE = driver("Zo súboru");

    private static final TokenSlot OMNIKEY_ZEP = new TokenSlot(1, 0, "Sig_ZEP", "Idemia", "Cosmo", "ffffffff", "HID Global OMNIKEY 5422 Smartcard Reader; Sig_ZEP");
    private static final TokenSlot OMNIKEY_EP = new TokenSlot(2, 1, "Sig_EP", "Idemia", "Cosmo", "ffffffff", "HID Global OMNIKEY 5422 Smartcard Reader; Sig_EP");
    private static final TokenSlot ACS_ZEP = new TokenSlot(3, 2, "Sig_ZEP", "Idemia", "Cosmo", "ffffffff", "ACS ACR38U-CCID; Sig_ZEP");
    private static final TokenSlot GEMALTO_1 = new TokenSlot(0, 0, "Card #6E1E37488C911948", "Gemalto", "IDPrime", "6E1E37488C911948", "ACS ACR38U-CCID");
    private static final TokenSlot GEMALTO_2 = new TokenSlot(1, 1, "Card #6E1E37488C911948 (Digital", "Gemalto", "IDPrime", "6E1E37488C911948", "ACS ACR38U-CCID (Digital Signature Pin)");

    @Test
    void testSingleCardIsDescribedByDriverOnly() {
        var descriptions = TokenOptionDescription.describeAll(List.of(
                new TokenOption(EID, OMNIKEY_ZEP),
                new TokenOption(KEYSTORE, null)));

        assertEquals(List.of(
                new TokenOptionDescription("Občiansky preukaz", null, null, null),
                new TokenOptionDescription("Zo súboru", null, null, null)), descriptions);
    }

    @Test
    void testCardsOfTheSameDriverAreDescribedByReader() {
        var descriptions = TokenOptionDescription.describeAll(List.of(
                new TokenOption(EID, OMNIKEY_ZEP),
                new TokenOption(EID, ACS_ZEP)));

        assertEquals(List.of(
                new TokenOptionDescription("Občiansky preukaz", "HID Global OMNIKEY 5422 Smartcard Reader", null, null),
                new TokenOptionDescription("Občiansky preukaz", "ACS ACR38U-CCID", null, null)), descriptions);
    }

    @Test
    void testSlotsOfOneCardAreNumbered() {
        var descriptions = TokenOptionDescription.describeAll(List.of(
                new TokenOption(EID, OMNIKEY_ZEP),
                new TokenOption(GEMALTO, GEMALTO_1),
                new TokenOption(GEMALTO, GEMALTO_2)));

        assertEquals(List.of(
                new TokenOptionDescription("Občiansky preukaz", null, null, null),
                new TokenOptionDescription("Gemalto IDPrime 940", null, 1, null),
                new TokenOptionDescription("Gemalto IDPrime 940", null, 2, null)), descriptions);
    }

    @Test
    void testSlotsAndReadersTogether() {
        var descriptions = TokenOptionDescription.describeAll(List.of(
                new TokenOption(EID, OMNIKEY_ZEP),
                new TokenOption(EID, OMNIKEY_EP),
                new TokenOption(EID, ACS_ZEP)));

        assertEquals(List.of(
                new TokenOptionDescription("Občiansky preukaz", "HID Global OMNIKEY 5422 Smartcard Reader", null, true),
                new TokenOptionDescription("Občiansky preukaz", "HID Global OMNIKEY 5422 Smartcard Reader", null, false),
                new TokenOptionDescription("Občiansky preukaz", "ACS ACR38U-CCID", null, null)), descriptions);
    }

    @Test
    void testEidSlotsAreQualifiedAndNonQualified() {
        var descriptions = TokenOptionDescription.describeAll(List.of(
                new TokenOption(EID, OMNIKEY_ZEP),
                new TokenOption(EID, OMNIKEY_EP)));

        assertEquals(List.of(
                new TokenOptionDescription("Občiansky preukaz", null, null, true),
                new TokenOptionDescription("Občiansky preukaz", null, null, false)), descriptions);
    }

    private static PKCS11TokenDriver driver(String name) {
        return new PKCS11TokenDriver(name, Path.of(""), name, "");
    }
}
