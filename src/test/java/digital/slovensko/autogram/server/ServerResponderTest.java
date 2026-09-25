package digital.slovensko.autogram.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.ResourceBundle;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Guards the HTTP contract of the signing endpoints for the events of the new
 * single {@code SigningResponder} port.
 */
class ServerResponderTest {

    @BeforeAll
    static void initErrorResponses() {
        ErrorResponseBuilder.init(
                ResourceBundle.getBundle("digital.slovensko.autogram.ui.gui.language.l10n"));
    }

    @Test
    void canceledDocumentResponds204() throws Exception {
        var exchange = mockExchange();

        new ServerResponder(exchange).onDocumentCanceled();

        verify(exchange).sendResponseHeaders(204, 0);
    }

    @Test
    void skippedDocumentResponds204() throws Exception {
        var exchange = mockExchange();

        new ServerResponder(exchange).onDocumentSkipped();

        verify(exchange).sendResponseHeaders(204, 0);
    }

    @Test
    void skippedRemainingDocumentResponds204() throws Exception {
        var exchange = mockExchange();

        new ServerResponder(exchange).onDocumentSkippedRemaining();

        verify(exchange).sendResponseHeaders(204, 0);
    }

    @Test
    void startedBatchRespondsBatchId() throws Exception {
        var exchange = mockExchange();
        var batch = new Batch(1);
        batch.start(null);

        new BatchServerResponder(exchange).onBatchStarted(batch, SigningMode.BULK);

        verify(exchange).sendResponseHeaders(200, 0);
    }

    private static HttpExchange mockExchange() throws Exception {
        var exchange = mock(HttpExchange.class);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        return exchange;
    }
}
