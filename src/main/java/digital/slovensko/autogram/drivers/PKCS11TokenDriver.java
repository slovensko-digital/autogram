package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.DefaultDriverDetector.TokenDriverShortnames;
import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PKCS11TokenDriver extends TokenDriver {
    public PKCS11TokenDriver(String name, Path path, String shortname, String noKeysHelperText) {
        super(name, path, shortname, noKeysHelperText);
    }

    public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
        Pkcs11TokenSlots.finalizeOnExit(getPath().toString());
        return new NativePkcs11SignatureToken(getPath().toString(), pm, settings, settings.getDriverSlotIndex(getShortname()));
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings, TokenSlot slot) {
        if (slot == null)
            return createToken(pm, settings);

        Logging.log("Using " + slot + " of " + getPath());
        Pkcs11TokenSlots.finalizeOnExit(getPath().toString());
        if (slot.slotId() <= Integer.MAX_VALUE)
            return new NativePkcs11SignatureToken(getPath().toString(), pm, settings, (int) slot.slotId(), -1);

        return new NativePkcs11SignatureToken(getPath().toString(), pm, settings, -1, slot.slotListIndex());
    }

    @Override
    public Optional<List<TokenSlot>> getSlotsWithToken(SignatureTokenSettings settings) {
        // slot index set explicitly by the user takes precedence
        if (settings.getDriverSlotIndex(getShortname()) >= 0)
            return Optional.empty();

        try {
            return Optional.of(Pkcs11TokenSlots.listSlotsWithToken(getPath().toString()));
        } catch (Exception e) {
            Logging.log("Unable to list slots of " + getPath() + ": " + e);
            return Optional.of(List.of());
        }
    }

    /**
     * eID klient fails every login with CKR_FUNCTION_FAILED once another driver talked to the card after it, e.g.
     * MONET+ ProID+Q looking for its cards. It works again only after the module is initialized again. The login fails
     * before eID klient asks for the PIN, and when retried, eID klient asks for it itself, so no PIN is sent twice.
     */
    @Override
    public Optional<AbstractKeyStoreTokenConnection> recoverToken(PasswordManager pm, SignatureTokenSettings settings, TokenSlot slot, DSSException error) {
        if (!getShortname().equals(TokenDriverShortnames.EID) || !"CKR_FUNCTION_FAILED".equals(getRootCauseMessage(error)))
            return Optional.empty();

        var pkcs11Path = getPath().toString();
        try {
            Pkcs11TokenSlots.reinitializeModule(pkcs11Path);
            if (slot == null)
                return Optional.of(createToken(pm, settings));

            // slot IDs are assigned again
            var sameSlot = Pkcs11TokenSlots.listSlotsWithToken(pkcs11Path).stream()
                    .filter((s) -> s.label().equals(slot.label()) && s.serialNumber().equals(slot.serialNumber()) && s.readerName().equals(slot.readerName()))
                    .findFirst();
            return sameSlot.map((s) -> createToken(pm, settings, s));
        } catch (Exception e) {
            Logging.log("Unable to recover " + pkcs11Path + ": " + e);
            return Optional.empty();
        }
    }

    private static String getRootCauseMessage(Throwable e) {
        while (e.getCause() != null && e.getCause() != e)
            e = e.getCause();

        return e.getMessage();
    }

    @Override
    public boolean needsInsertedCard() {
        // a custom driver may have software tokens, e.g. SoftHSM
        return !getShortname().equals(TokenDriverShortnames.CUSTOM_PKCS11);
    }
}
