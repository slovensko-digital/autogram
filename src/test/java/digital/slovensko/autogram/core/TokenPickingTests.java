package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.drivers.CardReaders;
import digital.slovensko.autogram.drivers.PKCS11TokenDriver;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.drivers.TokenSlot;
import digital.slovensko.autogram.server.CertificatesResponder;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TokenPickingTests {
    private static final TokenSlot EID_ZEP = new TokenSlot(1, 0, "Sig_ZEP", "Idemia", "Cosmo", "ffffffff", "Reader A; Sig_ZEP");
    private static final TokenSlot EID_EP = new TokenSlot(2, 1, "Sig_EP", "Idemia", "Cosmo", "ffffffff", "Reader A; Sig_EP");
    private static final TokenSlot EID2_ZEP = new TokenSlot(3, 2, "Sig_ZEP", "Idemia", "Cosmo", "ffffffff", "Reader B; Sig_ZEP");
    private static final TokenSlot EID2_EP = new TokenSlot(4, 3, "Sig_EP", "Idemia", "Cosmo", "ffffffff", "Reader B; Sig_EP");
    private static final TokenSlot ICA_SLOT = new TokenSlot(5, 4, "I.CA", "I.CA", "SecureStore", "1234", "Reader C");

    @Test
    void testTokensOfAllDriversAreOfferedTogether() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID_EP, EID2_ZEP, EID2_EP)));
        var ica = new FakeDriver("secure_store", Optional.of(List.of(ICA_SLOT)));
        var gemalto = new FakeDriver("gemalto", Optional.of(List.of()));
        var keystore = new FakeDriver("keystore", Optional.empty());
        var ui = new PickingUI((options) -> options.found().get(2));
        var autogram = new Autogram(ui, new FakeSettings(eid, ica, gemalto, keystore));

        var signingKeys = new ArrayList<SigningKey>();
        autogram.pickSigningKeyAndThen(signingKeys::add);

        assertEquals(List.of(
                new TokenOption(eid, EID_ZEP),
                new TokenOption(eid, EID2_ZEP),
                new TokenOption(ica, ICA_SLOT),
                new TokenOption(keystore, null)), ui.offered.found());
        assertEquals(List.of(gemalto), ui.offered.otherDrivers());
        assertEquals(List.of(ICA_SLOT), ica.usedSlots);
        assertTrue(eid.usedSlots.isEmpty());
        assertEquals(1, signingKeys.size());
    }

    @Test
    void testEidEpSlotsAreOfferedWhenEnabled() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID_EP)));
        var settings = new FakeSettings(eid);
        settings.setEidEpSlotsEnabled(true);
        var ui = new PickingUI((options) -> options.found().get(1));
        var autogram = new Autogram(ui, settings);

        autogram.pickSigningKeyAndThen(key -> {});

        assertEquals(List.of(new TokenOption(eid, EID_ZEP), new TokenOption(eid, EID_EP)), ui.offered.found());
        assertEquals(List.of(EID_EP), eid.usedSlots);
    }

    @Test
    void testOtherDriverCanBePicked() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID2_ZEP)));
        var gemalto = new FakeDriver("gemalto", Optional.of(List.of()));
        var ui = new PickingUI((options) -> new TokenOption(options.otherDrivers().get(0), null));
        var autogram = new Autogram(ui, new FakeSettings(eid, gemalto));

        autogram.pickSigningKeyAndThen(key -> {});

        assertEquals(1, gemalto.usedSlots.size());
        assertEquals(null, gemalto.usedSlots.get(0));
    }

    @Test
    void testOnlyTokenIsUsedWithoutAsking() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID_EP)));
        var gemalto = new FakeDriver("gemalto", Optional.of(List.of()));
        var keystore = new FakeDriver("keystore", Optional.empty());
        var autogram = new Autogram(new NoPickingUI(), new FakeSettings(eid, gemalto, keystore));

        autogram.pickSigningKeyAndThen(key -> {});

        assertEquals(List.of(EID_ZEP), eid.usedSlots);
    }

    @Test
    void testDialogIsShownAfterUnfinishedAutomaticPick() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID_EP)));
        var gemalto = new FakeDriver("gemalto", Optional.of(List.of()));
        var keystore = new FakeDriver("keystore", Optional.empty());
        var ui = new PickingUI((options) -> options.found().get(0));
        var autogram = new Autogram(ui, new FakeSettings(eid, gemalto, keystore));
        var signingKeys = new ArrayList<SigningKey>();

        // the card is used automatically, but the user closes the certificate picker
        ui.pickKeys = false;
        autogram.pickSigningKeyAndThen(signingKeys::add);
        assertEquals(0, ui.pickTokenCalls);
        assertTrue(signingKeys.isEmpty());

        // next time they get to choose, the card and other drivers are offered
        ui.pickKeys = true;
        autogram.pickSigningKeyAndThen(signingKeys::add);
        assertEquals(1, ui.pickTokenCalls);
        assertEquals(List.of(new TokenOption(eid, EID_ZEP), new TokenOption(keystore, null)), ui.offered.found());
        assertEquals(List.of(gemalto), ui.offered.otherDrivers());
        assertEquals(1, signingKeys.size());

        // having finished with a key, the card is used automatically again
        autogram.pickSigningKeyAndThen(signingKeys::add);
        assertEquals(1, ui.pickTokenCalls);
        assertEquals(List.of(EID_ZEP, EID_ZEP, EID_ZEP), eid.usedSlots);
        assertEquals(2, signingKeys.size());
    }

    @Test
    void testAllDriversAreOfferedWhenSearchTimesOut() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID2_ZEP)));
        var hanging = new FakeDriver("hanging", Optional.of(List.of(ICA_SLOT)));
        hanging.delayMillis = 2000;
        var keystore = new FakeDriver("keystore", Optional.empty());
        var ui = new PickingUI((options) -> options.found().get(0));
        var autogram = new Autogram(ui, new FakeSettings(eid, hanging, keystore), 200, () -> CardReaders.State.UNKNOWN);

        autogram.pickSigningKeyAndThen(key -> {});

        assertEquals(List.of(new TokenOption(eid, null), new TokenOption(hanging, null), new TokenOption(keystore, null)),
                ui.offered.found());
        assertEquals(List.of(), ui.offered.otherDrivers());
        assertEquals(1, eid.usedSlots.size());
        assertEquals(null, eid.usedSlots.get(0));
    }

    @Test
    void testEobcankaAndMonetAreNotSearched() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID2_ZEP)));
        var eobcanka = new FakeDriver("cz_eid", Optional.of(List.of(ICA_SLOT)));
        var monet = new FakeDriver("monet", Optional.of(List.of(ICA_SLOT)));
        var gemalto = new FakeDriver("gemalto", Optional.of(List.of()));
        var ui = new PickingUI((options) -> options.found().get(0));
        var autogram = new Autogram(ui, new FakeSettings(eid, eobcanka, monet, gemalto));

        autogram.pickSigningKeyAndThen(key -> {});

        assertFalse(eobcanka.searched);
        assertFalse(monet.searched);
        assertEquals(List.of(new TokenOption(eid, EID_ZEP), new TokenOption(eid, EID2_ZEP)), ui.offered.found());
        assertEquals(List.of(eobcanka, monet, gemalto), ui.offered.otherDrivers());
    }

    @Test
    void testTokenIsConnectedAgainWhenDriverRecovers() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP)));
        eid.failingConnections = 1;
        eid.recoverable = true;
        var autogram = new Autogram(new NoPickingUI(), new FakeSettings(eid));

        var signingKeys = new ArrayList<SigningKey>();
        autogram.pickSigningKeyAndThen(signingKeys::add);

        assertEquals(1, signingKeys.size());
        assertEquals(1, eid.recoveries);
        assertEquals(1, eid.closedFailingConnections);
    }

    @Test
    void testErrorIsShownWhenDriverCannotRecover() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP)));
        eid.failingConnections = 1;
        var autogram = new Autogram(new NoPickingUI(), new FakeSettings(eid));

        assertThrows(PINIncorrectException.class, () -> autogram.pickSigningKeyAndThen(key -> {}));
        assertEquals(0, eid.recoveries);
    }

    @Test
    void testPkcs11DriverRecoversOnlyEidFailingFunction() {
        var settings = new UserSettings();
        var functionFailed = new DSSException("load failed", new IllegalStateException("CKR_FUNCTION_FAILED"));
        var pinIncorrect = new DSSException("load failed", new IllegalStateException("CKR_PIN_INCORRECT"));
        var gemalto = new PKCS11TokenDriver("Gemalto", Path.of("/nonexistent/libpkcs11.so"), "gemalto", "");
        var eid = new PKCS11TokenDriver("eID", Path.of("/nonexistent/libpkcs11.so"), "eid", "");

        assertEquals(Optional.empty(), gemalto.recoverToken(null, settings, EID_ZEP, functionFailed));
        assertEquals(Optional.empty(), eid.recoverToken(null, settings, EID_ZEP, pinIncorrect));
        // the module can't be initialized again
        assertEquals(Optional.empty(), eid.recoverToken(null, settings, EID_ZEP, functionFailed));
    }

    @Test
    void testCardDriversAreNotSearchedWithoutInsertedCard() {
        var eid = cardDriver("eid", Optional.of(List.of(EID_ZEP)));
        var gemalto = cardDriver("gemalto", Optional.of(List.of()));
        var keystore = new FakeDriver("keystore", Optional.empty());
        var ui = new PickingUI((options) -> options.found().get(0));
        var autogram = new Autogram(ui, new FakeSettings(eid, gemalto, keystore), 10_000, () -> CardReaders.State.NO_CARD);

        autogram.pickSigningKeyAndThen(key -> {});

        assertFalse(eid.searched);
        assertFalse(gemalto.searched);
        assertTrue(keystore.searched);
        assertEquals(List.of(new TokenOption(keystore, null)), ui.offered.found());
        assertEquals(List.of(eid, gemalto), ui.offered.otherDrivers());
    }

    @Test
    void testNoDriverIsSearchedWhenCardReadersDoNotRespond() {
        var eid = cardDriver("eid", Optional.of(List.of(EID_ZEP)));
        var gemalto = cardDriver("gemalto", Optional.of(List.of()));
        var keystore = new FakeDriver("keystore", Optional.empty());
        var ui = new PickingUI((options) -> options.found().get(0));
        var autogram = new Autogram(ui, new FakeSettings(eid, gemalto, keystore), 10_000, () -> CardReaders.State.NOT_RESPONDING);

        autogram.pickSigningKeyAndThen(key -> {});

        assertFalse(eid.searched);
        assertFalse(gemalto.searched);
        assertFalse(keystore.searched);
        assertEquals(List.of(new TokenOption(eid, null), new TokenOption(gemalto, null), new TokenOption(keystore, null)),
                ui.offered.found());
        assertEquals(List.of(), ui.offered.otherDrivers());
    }

    @Test
    void testCardDriversAreSearchedWhenCardIsInsertedOrCardsAreUnknown() {
        for (var cardReaders : List.of(CardReaders.State.CARD_PRESENT, CardReaders.State.UNKNOWN)) {
            var eid = cardDriver("eid", Optional.of(List.of(EID_ZEP)));
            var keystore = new FakeDriver("keystore", Optional.empty());
            var ui = new PickingUI((options) -> options.found().get(0));
            var autogram = new Autogram(ui, new FakeSettings(eid, keystore), 10_000, () -> cardReaders);

            autogram.pickSigningKeyAndThen(key -> {});

            assertEquals(List.of(EID_ZEP), eid.usedSlots, cardReaders.name());
        }
    }

    @Test
    void testCardReadersAreNotCheckedWithoutCardDrivers() {
        var keystore = new FakeDriver("keystore", Optional.empty());
        var eobcanka = cardDriver("cz_eid", Optional.of(List.of(ICA_SLOT)));
        var autogram = new Autogram(new PickingUI((options) -> options.found().get(0)), new FakeSettings(keystore, eobcanka),
                10_000, () -> {
                    throw new AssertionError("Card readers should not be checked");
                });

        autogram.pickSigningKeyAndThen(key -> {});

        assertTrue(keystore.searched);
    }

    @Test
    void testPkcs11DriversNeedInsertedCardExceptCustomOne() {
        assertTrue(new PKCS11TokenDriver("eID", Path.of(""), "eid", "").needsInsertedCard());
        assertFalse(new PKCS11TokenDriver("Custom", Path.of(""), "custom_pkcs11", "").needsInsertedCard());
    }

    @Test
    void testUsedTokenIsRememberedOnlyWhenKeyIsPicked() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID2_ZEP)));
        var settings = new FakeSettings(eid);
        var ui = new PickingUI((options) -> options.found().get(1));
        var autogram = new Autogram(ui, settings);

        ui.pickKeys = false;
        autogram.pickSigningKeyAndThen(key -> {});
        assertEquals(Optional.empty(), settings.getLastUsedToken());

        ui.pickKeys = true;
        autogram.pickSigningKeyAndThen(key -> {});
        assertEquals(Optional.of(LastUsedToken.of(new TokenOption(eid, EID2_ZEP))), settings.getLastUsedToken());
    }

    @Test
    void testOnlyDriverWithoutTokensIsUsedWithoutAsking() {
        var eid = new FakeDriver("eid", Optional.of(List.of()));
        var autogram = new Autogram(new NoPickingUI(), new FakeSettings(eid));

        autogram.pickSigningKeyAndThen(key -> {});

        assertEquals(1, eid.usedSlots.size());
        assertEquals(null, eid.usedSlots.get(0));
    }

    @Test
    void testFailingDriverIsOfferedAmongOtherDrivers() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID2_ZEP)));
        var broken = new FakeDriver("broken", null);
        var ui = new PickingUI((options) -> options.found().get(0));
        var autogram = new Autogram(ui, new FakeSettings(eid, broken));

        autogram.pickSigningKeyAndThen(key -> {});

        assertEquals(List.of(broken), ui.offered.otherDrivers());
        assertEquals(List.of(EID_ZEP), eid.usedSlots);
    }

    @Test
    void testGetCertificatesFromPickedToken() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID_EP, EID2_ZEP, EID2_EP)));
        var ui = new PickingUI((options) -> options.found().get(1));
        var autogram = new Autogram(ui, new FakeSettings(eid));
        var responder = mock(CertificatesResponder.class);

        autogram.getCertificates(responder, List.of(eid), () -> {});

        assertEquals(List.of(EID2_ZEP), eid.usedSlots);
        verify(responder).onSuccess(any());
    }

    @Test
    void testGetCertificatesCanceledInPicker() {
        var eid = new FakeDriver("eid", Optional.of(List.of(EID_ZEP, EID2_ZEP)));
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void pickTokenAndThen(TokenOptions options, Consumer<TokenOption> callback, Runnable onCancel) {
                onCancel.run();
            }
        };
        var autogram = new Autogram(ui, new FakeSettings(eid));
        var responder = mock(CertificatesResponder.class);
        var consentDialogClosed = new ArrayList<Boolean>();

        autogram.getCertificates(responder, List.of(eid), () -> consentDialogClosed.add(true));

        assertTrue(eid.usedSlots.isEmpty());
        assertEquals(List.of(true), consentDialogClosed);
        verify(responder).onError(any(SigningCanceledByUserException.class));
        verify(responder, never()).onSuccess(any());
    }

    @Test
    void testOptionsForDriver() {
        var eid = new FakeDriver("eid", Optional.empty());
        var gemalto = new FakeDriver("gemalto", Optional.empty());
        var options = new TokenOptions(List.of(new TokenOption(eid, EID_ZEP), new TokenOption(eid, EID2_ZEP)), List.of(gemalto));

        assertEquals(new TokenOptions(List.of(), List.of(gemalto)), options.forDriver("gemalto").orElseThrow());
        assertEquals(Optional.empty(), options.forDriver("monet"));
    }

    @Test
    void testAutomaticOption() {
        var eid = new FakeDriver("eid", Optional.empty());
        var gemalto = new FakeDriver("gemalto", Optional.empty());
        var keystore = new FakeDriver("keystore", Optional.empty());
        var zep = new TokenOption(eid, EID_ZEP);
        var zep2 = new TokenOption(eid, EID2_ZEP);
        var keystoreOption = new TokenOption(keystore, null);

        assertEquals(Optional.of(zep), new TokenOptions(List.of(zep, keystoreOption), List.of(gemalto)).getAutomaticOption());
        assertEquals(Optional.empty(), new TokenOptions(List.of(zep, zep2), List.of()).getAutomaticOption());
        assertEquals(Optional.of(keystoreOption), new TokenOptions(List.of(keystoreOption), List.of()).getAutomaticOption());
        assertEquals(Optional.empty(), new TokenOptions(List.of(keystoreOption), List.of(gemalto)).getAutomaticOption());
        assertEquals(Optional.of(new TokenOption(gemalto, null)), new TokenOptions(List.of(), List.of(gemalto)).getAutomaticOption());
    }

    @Test
    void testPkcs11DriverDoesNotListTokensWhenSlotIndexIsSet() {
        var settings = new UserSettings();
        settings.setDriverSlotIndex("custom", 2);
        var driver = new PKCS11TokenDriver("custom", Path.of("/nonexistent/libpkcs11.so"), "custom", "");

        assertEquals(Optional.empty(), driver.getSlotsWithToken(settings));
    }

    @Test
    void testPkcs11DriverFindsNoTokensWhenLibraryCannotBeLoaded() {
        var settings = new UserSettings();
        var driver = new PKCS11TokenDriver("custom", Path.of("/nonexistent/libpkcs11.so"), "custom", "");

        assertEquals(Optional.of(List.of()), driver.getSlotsWithToken(settings));
    }

    private static FakeDriver cardDriver(String shortname, Optional<List<TokenSlot>> slots) {
        var driver = new FakeDriver(shortname, slots);
        driver.needsCard = true;
        return driver;
    }

    private static class PickingUI extends TestAutogramFactory.FakeUI {
        private final Function<TokenOptions, TokenOption> pick;
        TokenOptions offered;
        int pickTokenCalls = 0;
        boolean pickKeys = true;

        PickingUI(Function<TokenOptions, TokenOption> pick) {
            this.pick = pick;
        }

        @Override
        public void pickTokenAndThen(TokenOptions options, Consumer<TokenOption> callback, Runnable onCancel) {
            offered = options;
            pickTokenCalls++;
            callback.accept(pick.apply(options));
        }

        @Override
        public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback) {
            if (pickKeys)
                super.pickKeyAndThen(keys, driver, callback);
        }
    }

    private static class NoPickingUI extends TestAutogramFactory.FakeUI {
        @Override
        public void pickTokenAndThen(TokenOptions options, Consumer<TokenOption> callback, Runnable onCancel) {
            throw new AssertionError("User should not be asked to pick a token");
        }
    }

    private static class FakeDriver extends TokenDriver {
        private final Optional<List<TokenSlot>> slots;
        final List<TokenSlot> usedSlots = new ArrayList<>();
        long delayMillis = 0;
        boolean needsCard = false;
        boolean searched = false;
        /** number of connections that fail to read keys, before the working ones */
        int failingConnections = 0;
        int closedFailingConnections = 0;
        boolean recoverable = false;
        int recoveries = 0;

        /**
         * @param slots tokens found by the driver, null to fail when searching for them
         */
        FakeDriver(String shortname, Optional<List<TokenSlot>> slots) {
            super(shortname, Path.of(""), shortname, "");
            this.slots = slots;
        }

        @Override
        public Optional<List<TokenSlot>> getSlotsWithToken(SignatureTokenSettings settings) {
            searched = true;
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (slots == null)
                throw new IllegalStateException("driver crashed");

            return slots;
        }

        @Override
        public boolean needsInsertedCard() {
            return needsCard;
        }

        @Override
        public Optional<AbstractKeyStoreTokenConnection> recoverToken(PasswordManager pm, SignatureTokenSettings settings, TokenSlot slot, DSSException error) {
            if (!recoverable)
                return Optional.empty();

            recoveries++;
            return Optional.of(createToken(pm, settings, slot));
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings, TokenSlot slot) {
            usedSlots.add(slot);
            return createToken(pm, settings);
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
            try {
                var keystore = Objects.requireNonNull(getClass().getResource("test.keystore")).getFile();
                var password = new KeyStore.PasswordProtection("".toCharArray());
                if (failingConnections > 0) {
                    failingConnections--;
                    return new Pkcs12SignatureToken(keystore, password) {
                        @Override
                        public List<DSSPrivateKeyEntry> getKeys() {
                            throw new DSSException("load failed", new IllegalStateException("CKR_FUNCTION_FAILED"));
                        }

                        @Override
                        public void close() {
                            closedFailingConnections++;
                            super.close();
                        }
                    };
                }

                return new Pkcs12SignatureToken(keystore, password);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FakeSettings extends UserSettings {
        private final List<TokenDriver> drivers;

        FakeSettings(TokenDriver... drivers) {
            this.drivers = List.of(drivers);
        }

        @Override
        public DriverDetector getDriverDetector() {
            return () -> drivers;
        }
    }
}
