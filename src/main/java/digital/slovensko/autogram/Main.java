package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AppStarter;

import java.util.Arrays;

import static java.util.Objects.requireNonNullElse;

public class Main {
    public static void main(String[] args) {
        AppStarter.start(args);
    }

    public static String getVersion() {
        return requireNonNullElse(System.getProperty("jpackage.app-version"), "dev");
    }
}
