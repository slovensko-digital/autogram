package com.octosign.whitelabel.signing;

import java.io.File;

public class Driver {
    public enum StorageType {
        SINGLE,
        MANY
    }

    private String name;
    private File driverFile;
    private StorageType type;

    public Driver(String name, String path, StorageType type) {
        this.name = name;
        this.type = type;
        driverFile = new File(path);
    }

    public String getName() {
        return name;
    }

    public File getDriverFile() {
        return driverFile;
    }

    public StorageType getType() {
        return type;
    }
}
