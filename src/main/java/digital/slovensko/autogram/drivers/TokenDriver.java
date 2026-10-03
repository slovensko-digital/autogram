package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class TokenDriver {
    protected final String name;
    private final Path path;
    private final String shortname;
    private final String noKeysHelperText;

    public TokenDriver(String name, Path path, String shortname, String noKeysHelperText) {
        this.name = name;
        this.path = path;
        this.shortname = shortname;
        this.noKeysHelperText = noKeysHelperText;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return this.path;
    }

    public boolean isInstalled() {
        return path.toFile().exists();
    }


    public abstract AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings);

    /**
     * Creates token connection to the given slot, or to the default one if slot is null.
     */
    public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings, TokenSlot slot) {
        return createToken(pm, settings);
    }

    /**
     * Lists tokens (cards) the user can choose from, possibly none. Empty optional if the driver doesn't list tokens
     * (e.g. keystore file, or slot set explicitly in settings) and the user should be offered the driver itself.
     */
    public Optional<List<TokenSlot>> getSlotsWithToken(SignatureTokenSettings settings) {
        return Optional.empty();
    }

    /**
     * Connects to the token again after reading its keys failed, if the driver can recover from the error.
     *
     * @param slot slot the failed connection used, null if the default one
     * @return new connection, empty if the error can't be recovered from
     */
    public Optional<AbstractKeyStoreTokenConnection> recoverToken(PasswordManager pm, SignatureTokenSettings settings, TokenSlot slot, DSSException error) {
        return Optional.empty();
    }

    /**
     * True if the driver's tokens are cards in PC/SC readers, so it can't find any token without an inserted card.
     */
    public boolean needsInsertedCard() {
        return false;
    }

    public String getShortname() {
        return shortname;
    }

    public String getNoKeysHelperText() {
        return noKeysHelperText;
    }
}
