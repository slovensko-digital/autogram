package digital.slovensko.autogram.server.dto;

public record BatchEndResponseBody(String status) {
    public static BatchEndResponseBody of(boolean allDocumentsProcessed) {
        return new BatchEndResponseBody(allDocumentsProcessed ? "FINISHED" : "NOT_FINISHED");
    }
}
