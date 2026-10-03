package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AppStarter;
import digital.slovensko.autogram.util.Version;

import static java.util.Objects.requireNonNullElse;

public class Main {
    private static String[] args = new String[0];

    public static void main(String[] args) {
        Main.args = args;
        AppStarter.start(args);
    }

    /** Program arguments the application was started with. */
    public static String[] getArgs() {
        return args.clone();
    }

    public static Version getVersion() {
        return Version.createFromVersionString(requireNonNullElse(System.getProperty("jpackage.app-version"), "dev"));
    }

    public static String getVersionString() {
        return getVersion().toString();
    }
}
