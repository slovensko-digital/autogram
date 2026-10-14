package digital.slovensko.autogram.server.dto;

import com.google.gson.Gson;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BatchRequestBodiesTest {
    @ParameterizedTest
    @ValueSource(strings = {"{\"totalNumberOfDocuments\": 0}", "{\"totalNumberOfDocuments\": -1}", "{}"})
    void batchWithoutDocumentsIsRejected(String json) {
        var body = new Gson().fromJson(json, BatchStartRequestBody.class);
        assertThrows(RequestValidationException.class, body::getTotalNumberOfDocuments);
    }

    @Test
    void batchWithDocumentsIsAccepted() {
        var body = new Gson().fromJson("{\"totalNumberOfDocuments\": 3}", BatchStartRequestBody.class);
        assertEquals(3, body.getTotalNumberOfDocuments());
    }

    @Test
    void batchEndRespondsWithStatus() {
        assertEquals("{\"status\":\"FINISHED\"}", new Gson().toJson(BatchEndResponseBody.of(true)));
        assertEquals("{\"status\":\"NOT_FINISHED\"}", new Gson().toJson(BatchEndResponseBody.of(false)));
    }
}
