package digital.slovensko.autogram.server.dto;

public class InfoResponse {
    private final String version;
    private final String status;

    public InfoResponse(String version, String status) {
        this.version = version;
        this.status = status;
    }

    public static String getStatus() {
        return "READY"; // TODO: check if server is ready
    }
}
