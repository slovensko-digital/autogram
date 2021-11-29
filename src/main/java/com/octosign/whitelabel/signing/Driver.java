package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.OS;
import com.octosign.whitelabel.ui.Selectable;

import java.io.File;

import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;

public class Driver implements Selectable {
    private final String name;
    private final OS os;
    private final String path;

    public Driver(String name, OS os, String path) {
        this.name = name;
        this.os = os;
        this.path = path;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public OS getOs() {
        return os;
    }

    public boolean isInstalled() {
        if (isNullOrBlank(path))
            return false;

        return new File(path).exists();
    }

    public boolean isCompatible() {
        return os == OS.current();
    }
}

