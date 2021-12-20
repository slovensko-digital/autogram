package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.SelectableItem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Driver implements SelectableItem {
    private final String name;
    private final KeystoreType keystoreType;
    private final Map<OperatingSystem, Path> files;

    private Driver(String name, KeystoreType keystoreType, Map<OperatingSystem, Path> files) {
        this.name = requireNonNull(name);
        this.keystoreType = requireNonNull(keystoreType);
        this.files = requireNonNull(files);
    }

    public String getName() {
        return name;
    }

    @Override
    public String getDisplayedName() {
        return getName();
    }

    @Override
    public String getSimpleName() {
        return getName();
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
        private final Map<OperatingSystem, Path> items;

        public Builder(String name) {
            this.name = name;
            this.items = new HashMap<>();
        }

        public Builder file(OperatingSystem os, String path) {
            return file(os, Path.of(path));
        }

        public Builder file(OperatingSystem os, Path path) {
            items.put(os, path);
            return this;
        }

        public Driver keystore(KeystoreType keystoreType) {
            return new Driver(name, keystoreType, items);
        }
    }
}
