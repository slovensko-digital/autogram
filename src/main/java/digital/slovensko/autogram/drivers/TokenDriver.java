package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.nio.file.Path;

public abstract class TokenDriver {
    protected final String name;
    private final Path path;
    private final boolean needsPassword;

    public TokenDriver(String name, Path path, boolean needsPassword) {
        this.name = name;
        this.path = path;
        this.needsPassword = needsPassword;
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



    public abstract AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password);

    public boolean needsPassword() {
        return needsPassword;
    }
}
