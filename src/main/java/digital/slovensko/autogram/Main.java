package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AppStarter;
import digital.slovensko.autogram.util.Version;

import static java.util.Objects.requireNonNullElse;

public class Main {
    public static void main(String[] args) {
        AppStarter.start(args);
    }

    public static Version getVersion() {
        return Version.createFromVersionString(requireNonNullElse(System.getProperty("jpackage.app-version"), "dev"));
    }

    public static String getVersionString() {
        return getVersion().toString();
    }
}
