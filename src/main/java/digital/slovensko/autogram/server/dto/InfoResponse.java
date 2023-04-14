package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.Main;
import static java.util.Objects.requireNonNullElse;

public class InfoResponse {
    private final String version;
    private final String status;

    public InfoResponse(String version, String status) {
        this.version = version;
        this.status = status;
    }

    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }

    public static String getStatus() {
        return "READY"; // TODO: check if server is ready
    }
}
