package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.SelectableItem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Driver implements SelectableItem {
    private final String name;
    private final API api;
    private final Map<OperatingSystem, Path> files;

    private Driver(String name, API api, Map<OperatingSystem, Path> files) {
        this.name = requireNonNull(name);
        this.api = requireNonNull(api);
        this.files = requireNonNull(files);
   }

    public String getName() {
        return name;
    }

    @Override
    public String getSimpleName() {
        return getName();
    }

    public API getAPI() {
        return api;
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

    public Token createToken() {
        return api.createToken(this);
    }

    public static Driver.Builder of(String name) {
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
            items.put(os, Path.of(path));
            return this;
        }

        public Driver with(API api) {
            return new Driver(name, api, items);
        }
    }
}