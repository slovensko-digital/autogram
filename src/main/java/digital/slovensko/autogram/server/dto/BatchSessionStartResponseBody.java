package digital.slovensko.autogram.server.dto;

public class BatchSessionStartResponseBody {

    private final String batchId;

    public BatchSessionStartResponseBody(String batchId) {
        this.batchId = batchId;
    }

    public String getBatchId() {
        return batchId;
    }
}