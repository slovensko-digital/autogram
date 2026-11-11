package digital.slovensko.autogram.core;

import com.google.gson.Gson;
import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.NotEnoughMemoryException;
import digital.slovensko.autogram.server.ErrorResponseBuilder;
import digital.slovensko.autogram.ui.SupportedLanguage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * A document too large for the available memory: the signing ends with an error the user and the API client learn
 * about, instead of an OutOfMemoryError that ends the signing thread and leaves both waiting.
 */
class NotEnoughMemoryTest {

    /** The signing window gets the error instead of staying on "Signing in progress". */
    @Test
    void userLearnsThatMemoryRanOut() {
        var shownError = new AtomicReference<AutogramException>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void onSigningFailed(AutogramException e) {
                shownError.set(e);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var job = spy(TestSigningJobFactory.create(new NoBatch(), mock(Responder.class)));
        doThrow(new OutOfMemoryError("Java heap space")).when(job).signWithKey(any(), any());

        autogram.sign(job, mock(SigningKey.class));

        var error = assertInstanceOf(NotEnoughMemoryException.class, shownError.get());
        assertInstanceOf(OutOfMemoryError.class, error.getCause());
    }

    /** In a bulk batch the API client gets the error for the document, the rest of the batch can go on. */
    @Test
    void batchClientLearnsThatMemoryRanOut() {
        var batchRef = new AtomicReference<Batch>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void startBatch(Batch batch, Autogram autogram) {
                batchRef.set(batch);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        autogram.startBatchSigning(2, mock(BatchResponder.class));
        var batch = batchRef.get();
        autogram.signBatchWithKey(batch, mock(SigningKey.class));
        var responder = mock(Responder.class);
        var job = spy(TestSigningJobFactory.create(batch, responder));
        doThrow(new OutOfMemoryError("Java heap space")).when(job).signWithKey(any(), any());

        autogram.batchSign(job, batch.getBatchId());

        verify(responder).onDocumentSignFailed(any(NotEnoughMemoryException.class));
        assertEquals(1, batch.getProcessedDocumentsCount());
    }

    /** The API reports it with the existing code for an internal error, with the explanation in its message. */
    @Test
    void apiReportsItAsInternalError() {
        ErrorResponseBuilder.init(SupportedLanguage.ENGLISH.loadResources());

        var response = ErrorResponseBuilder.buildFromException(new NotEnoughMemoryException(new OutOfMemoryError()));

        var body = new Gson().toJsonTree(response.getBody()).getAsJsonObject();
        assertEquals(500, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", body.get("code").getAsString());
        assertEquals("Not enough memory", body.get("message").getAsString());
    }

    @Test
    void messageIsTranslated() {
        var error = new NotEnoughMemoryException(new OutOfMemoryError());

        assertEquals("Nedostatok pamäte", error.getSubheading(SupportedLanguage.SLOVAK.loadResources()));
        assertEquals("Not enough memory", error.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}
