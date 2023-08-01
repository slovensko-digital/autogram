package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AppStarter;
import digital.slovensko.autogram.util.Version;



public class Main {
    public static void main(String[] args) {
        AppStarter.start(args);
    }

    public static String getVersionString() {
        return Version.createCurrent().toString();
    }
}
