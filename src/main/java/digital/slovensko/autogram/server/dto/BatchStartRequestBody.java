package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;

public class BatchStartRequestBody {
    private final Integer totalNumberOfDocuments;

    public BatchStartRequestBody(int totalNumberOfDocuments) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;

    }

    public int getTotalNumberOfDocuments() {
        if (totalNumberOfDocuments == null)
            throw new RequestValidationException(MISSING_FIELD, "\"totalNumberOfDocuments\"");
        return totalNumberOfDocuments;
    }

}
