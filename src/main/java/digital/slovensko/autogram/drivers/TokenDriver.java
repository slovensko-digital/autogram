package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.util.OperatingSystem;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.nio.file.Path;
import java.util.List;

public abstract class TokenDriver {
    protected final String name;
    private final Path path;
    private final boolean needsPassword;
    private final String shortname;

    public TokenDriver(String name, Path path, boolean needsPassword, String shortname) {
        this.name = name;
        this.path = path;
        this.needsPassword = needsPassword;
        this.shortname = shortname;
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

    public String getShortname() {
        return shortname;
    }
}
