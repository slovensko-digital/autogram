package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.nio.file.Path;

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


    public abstract AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings, int driverSlotIndex);


    public String getShortname() {
        return shortname;
    }

    public String getNoKeysHelperText() {
        return noKeysHelperText;
    }
}
