package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;

public class BatchStartRequestBody {
    private final Integer totalNumberOfDocuments;

    public BatchStartRequestBody(int totalNumberOfDocuments) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;

    }

    public int getTotalNumberOfDocuments() {
        if (totalNumberOfDocuments == null)
            throw new RequestValidationException("\"totalNumberOfDocuments\" is required", "");
        return totalNumberOfDocuments;
    }

}
