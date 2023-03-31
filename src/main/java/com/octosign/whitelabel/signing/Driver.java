package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.picker.SelectableItem;
import digital.slovensko.autogram.util.OperatingSystem;

import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Driver implements SelectableItem {
    private final String name;
    private final String tokenType;
    private final KeystoreType keystoreType;
    private final Map<OperatingSystem, Path> files;

    private Driver(String name, String tokenType, KeystoreType keystoreType, Map<OperatingSystem, Path> files) {
        this.name = requireNonNull(name);
        this.tokenType = requireNonNull(tokenType);
        this.keystoreType = requireNonNull(keystoreType);
        this.files = requireNonNull(files);
    }

    public String getName() {
        return name;
    }

    public String getTokenType() {
        return tokenType;
    }

    @Override
    public String getDisplayedName() {
        return getName();
    }

    @Override
    public String getDisplayedDetails() {
        return getName() + " - " + getTokenType();
    }

    public KeystoreType getKeystoreType() {
        return keystoreType;
    }

    public Path getPath() {
        return getPathFor(OperatingSystem.current());
    }

    public String path() {
        return getPath() == null ? null : getPath().toString();
    }

    public Path getPathFor(OperatingSystem os) {
        return files.get(os);
    }

    public boolean isInstalled() {
        return getPath() != null && getPath().toFile().exists();
    }

    public boolean isCompatible() {
        return files.containsKey(OperatingSystem.current());
    }

    public static Driver.Builder name(String name) {
        return new Driver.Builder(name);
    }

    public static class Builder {
        private final String name;
        private String tokenType;
        private final Map<OperatingSystem, Path> items;

        public Builder(String name) {
            this.name = name;
            this.items = new HashMap<>();
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder file(OperatingSystem os, String path) {
            return file(os, Path.of(path));
        }

        public Builder file(OperatingSystem os, Path path) {
            items.put(os, path);
            return this;
        }

        public Driver keystore(KeystoreType keystoreType) {
            return new Driver(name, tokenType, keystoreType, items);
        }
    }
}
