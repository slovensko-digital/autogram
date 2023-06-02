package digital.slovensko.autogram.server.dto;

public class BatchSessionEndRequestBody {
    private final String batchId;

    public BatchSessionEndRequestBody(String batchId) {
        this.batchId = batchId;
    }

    public String getbatchId() {
        return batchId;
    }
}
