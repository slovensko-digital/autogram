package digital.slovensko.autogram.core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.server.ErrorResponseBuilder;
import digital.slovensko.autogram.server.ServerResponder;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.SupportedLanguage;
import digital.slovensko.autogram.ui.gui.BatchGuiFileResponder;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InteractiveBatchTest {
    private static class InteractiveUI extends TestAutogramFactory.FakeUI {
        @Override
        public void selectBatchMode(Batch batch, Consumer<SigningMode> onSelected, Runnable onCancel) {
            onSelected.accept(SigningMode.INTERACTIVE);
        }
    }

    @Test
    void modeIsSelectedBeforeStartingAndInteractiveDoesNotExpire() throws Exception {
        var batch = new SigningBatch(2);
        assertFalse(batch.isInteractive());
        batch.setMode(SigningMode.INTERACTIVE);
        batch.start(null);
        var expiration = SigningBatch.class.getDeclaredField("expirationDate");
        expiration.setAccessible(true);
        expiration.set(batch, new java.util.Date(0));
        assertFalse(batch.isExpired());
        batch.validate(batch.getBatchId());
        assertThrows(IllegalStateException.class, () -> batch.setMode(SigningMode.BULK));
        assertFalse(new NoBatch().isPresent());
    }

    @Test
    void contextSpecificPinIsCachedPerBatchAndClearedOnReset() {
        var prompts = new AtomicInteger();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public char[] getContextSpecificPassword() {
                prompts.incrementAndGet();
                return "1234".toCharArray();
            }
        };
        var manager = new PasswordManager(ui, new UserSettings());
        var batch = new SigningBatch(2);
        assertFalse(manager.isCachingPIN());
        var previousBatch = manager.setBatchContext(batch);
        try {
            assertTrue(manager.isCachingPIN());
            manager.getContextSpecificPassword();
            manager.getContextSpecificPassword();
        } finally {
            manager.setBatchContext(previousBatch);
        }
        assertFalse(manager.isCachingPIN());
        previousBatch = manager.setBatchContext(batch);
        try {
            manager.getContextSpecificPassword();
        } finally {
            manager.setBatchContext(previousBatch);
        }
        assertEquals(1, prompts.get());

        manager.reset();
        previousBatch = manager.setBatchContext(batch);
        try {
            manager.getContextSpecificPassword();
        } finally {
            manager.setBatchContext(previousBatch);
        }
        assertEquals(2, prompts.get());
    }

    @Test
    void uiSelectionStartsWithoutSharedKey() {
        var selected = new AtomicReference<Consumer<SigningMode>>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void selectBatchMode(Batch batch, Consumer<SigningMode> callback, Runnable onCancel) {
                selected.set(callback);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var responder = mock(BatchResponder.class);
        autogram.startBatchSigning(2, responder);
        verify(responder, never()).onBatchStartSuccess(any());
        selected.get().accept(SigningMode.INTERACTIVE);
        var captured = org.mockito.ArgumentCaptor.forClass(Batch.class);
        verify(responder).onBatchStartSuccess(captured.capture());
        assertTrue(captured.getValue().isInteractive());
        assertNull(captured.getValue().getSigningKey());
    }

    @Test
    void guiSubmitsOneFileAtATimeAndRecordsSkips(@TempDir Path directory) throws Exception {
        var first = Files.writeString(directory.resolve("first.txt"), "first").toFile();
        var second = Files.writeString(directory.resolve("second.txt"), "second").toFile();
        var third = Files.writeString(directory.resolve("third.txt"), "third").toFile();
        var shown = new ArrayList<SigningJob>();
        var result = new AtomicReference<BatchUiResult>();
        var ui = new InteractiveUI() {
            @Override
            public void startSigning(SigningJob job, Autogram autogram) {
                shown.add(job);
            }

            @Override
            public void onDocumentBatchSaved(BatchUiResult saved) {
                result.set(saved);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var params = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES,
                null, null, null, false, null, null, null, false, 640, true);
        autogram.startBatchSigning(3, new BatchGuiFileResponder(autogram, List.of(first, second, third),
                directory.resolve("signed"), params, EFormAttributes.build(params, true), false));
        assertEquals(1, shown.size());
        var batch = shown.get(0).getBatch();
        autogram.skipCurrentDocument(shown.get(0));
        assertEquals(2, shown.size());
        autogram.skipRemainingDocuments(shown.get(1));
        assertTrue(batch.isEnded());
        assertEquals(3, batch.getProcessedDocumentsCount());
        assertEquals(2, shown.size());
        assertNotNull(result.get());
        assertInstanceOf(SigningCanceledByUserException.class, result.get().getErrorsMap().get(first));
        assertInstanceOf(SigningCanceledByUserException.class, result.get().getErrorsMap().get(second));
        assertInstanceOf(SigningCanceledByUserException.class, result.get().getErrorsMap().get(third));
    }

    @Test
    void interactiveSuccessIsCountedBeforeResponder() {
        var shown = new AtomicReference<SigningJob>();
        var ui = new InteractiveUI() {
            @Override
            public void startSigning(SigningJob job, Autogram autogram) {
                shown.set(job);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var batchStartResponder = mock(BatchResponder.class);
        autogram.startBatchSigning(2, batchStartResponder);
        var captor = org.mockito.ArgumentCaptor.forClass(Batch.class);
        verify(batchStartResponder).onBatchStartSuccess(captor.capture());
        var batch = captor.getValue();
        var responder = mock(Responder.class);
        doAnswer(invocation -> {
            assertEquals(1, batch.getProcessedDocumentsCount());
            return null;
        }).when(responder).onDocumentSigned(any());
        var job = spy(TestSigningJobFactory.create(batch, responder));
        doReturn(mock(SignedDocument.class)).when(job).signWithKey(any(), any());
        autogram.batchSign(job, batch.getBatchId());
        assertSame(job, shown.get());
        autogram.sign(job, mock(SigningKey.class));
        verify(responder).onDocumentSigned(any());
    }

    @Test
    void cancelingRemainingInteractiveDocumentsRespondsWith204WithoutBody() throws Exception {
        ErrorResponseBuilder.init(SupportedLanguage.ENGLISH.loadResources());
        var autogram = TestAutogramFactory.create(new InteractiveUI());
        var batchResponder = mock(BatchResponder.class);
        autogram.startBatchSigning(3, batchResponder);
        var captor = org.mockito.ArgumentCaptor.forClass(Batch.class);
        verify(batchResponder).onBatchStartSuccess(captor.capture());
        var batch = captor.getValue();
        var exchange = mock(HttpExchange.class);
        var body = new ByteArrayOutputStream();
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(body);
        var job = TestSigningJobFactory.create(batch, new ServerResponder(exchange));
        autogram.batchSign(job, batch.getBatchId());

        autogram.skipRemainingDocuments(job);

        verify(exchange).sendResponseHeaders(204, 0);
        assertEquals(0, body.size());
        assertTrue(batch.isEnded());
        assertEquals(1, batch.getProcessedDocumentsCount());
    }

    @Test
    void interactiveSigningCachesPinAcrossWorkerThreadsUntilBatchEnds() throws Exception {
        var prompts = new AtomicInteger();
        var ui = new InteractiveUI() {
            @Override
            public char[] getContextSpecificPassword() {
                prompts.incrementAndGet();
                return "1234".toCharArray();
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var field = Autogram.class.getDeclaredField("passwordManager");
        field.setAccessible(true);
        var manager = (PasswordManager) field.get(autogram);
        var batchResponder = mock(BatchResponder.class);
        autogram.startBatchSigning(3, batchResponder);
        var captor = org.mockito.ArgumentCaptor.forClass(Batch.class);
        verify(batchResponder).onBatchStartSuccess(captor.capture());
        var batch = captor.getValue();
        var cachedPin = new AtomicReference<char[]>();

        try {
            for (var i = 0; i < 3; i++) {
                var responder = mock(Responder.class);
                var job = spy(TestSigningJobFactory.create(batch, responder));
                doAnswer(invocation -> {
                    assertTrue(manager.isCachingPIN());
                    var pin = manager.getContextSpecificPassword();
                    assertArrayEquals("1234".toCharArray(), pin);
                    if (cachedPin.get() == null) cachedPin.set(pin);
                    else assertSame(cachedPin.get(), pin);
                    return mock(SignedDocument.class);
                }).when(job).signWithKey(any(), any());
                autogram.batchSign(job, batch.getBatchId());
                var failure = new AtomicReference<Throwable>();
                var worker = new Thread(() -> {
                    try {
                        autogram.sign(job, mock(SigningKey.class));
                        assertFalse(manager.isCachingPIN());
                    } catch (Throwable e) {
                        failure.set(e);
                    }
                });
                worker.start();
                worker.join();
                assertNull(failure.get());
                verify(responder).onDocumentSigned(any());
                assertEquals(1, prompts.get());
            }
            assertTrue(batch.isEnded());
            assertArrayEquals(new char[4], cachedPin.get());
        } finally {
            autogram.shutdown();
        }
    }
}
