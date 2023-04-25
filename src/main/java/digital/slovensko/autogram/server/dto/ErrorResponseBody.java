package digital.slovensko.autogram.server.dto;

public class ErrorResponseBody {
    private final String code;
    private final String message;
    private final String details;

    public ErrorResponseBody(String code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}
