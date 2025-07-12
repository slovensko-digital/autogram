package digital.slovensko.autogram.server.dto;

import java.util.List;

public class InfoResponse {
    private final String version;
    private final String status;
    private final List<String> availableDrivers;

    public InfoResponse(String version, String status, List<String> availableDrivers) {
        this.version = version;
        this.status = status;
        this.availableDrivers = availableDrivers;
    }

    public static String getStatus() {
        return "READY"; // TODO: check if server is ready
    }
}
