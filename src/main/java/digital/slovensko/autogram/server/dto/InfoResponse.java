package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.Main;
import static java.util.Objects.requireNonNullElse;

public class InfoResponse {
    private final String version;
    private final String status;

    public InfoResponse() {
        this.version = getVersion();
        this.status = getStatus();
    }

    private static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }

    private static String getStatus() {
        return "READY"; // TODO: check if server is ready
    }
}
