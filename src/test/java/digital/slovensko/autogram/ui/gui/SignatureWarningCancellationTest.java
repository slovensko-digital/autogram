package digital.slovensko.autogram.ui.gui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParametersResolver;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.server.ErrorResponseBuilder;
import digital.slovensko.autogram.server.ServerResponder;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Run separately with -Dautogram.signatureWarningTests=true on a desktop or under xvfb-run. */
@EnabledIfSystemProperty(named = "autogram.signatureWarningTests", matches = "true")
class SignatureWarningCancellationTest {
    enum Warning { NOT_VALIDATED, INVALID }

    @BeforeAll
    static void startJavaFx() throws Exception {
        var ready = new FutureTask<Void>(() -> {
            Platform.setImplicitExit(false);
            ErrorResponseBuilder.init(SupportedLanguage.ENGLISH.loadResources());
            return null;
        });
        try {
            Platform.startup(ready);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @EnumSource(Warning.class)
    void cancelClosesBothDialogsAndCompletesPendingHttpRequest(Warning warning) throws Exception {
        try (var request = new PendingRequest()) {
            var dialog = onFxThread(() -> openDialog(warning, request.responder));
            try {
                assertFalse(request.response.isDone(), "The signing request must still be pending before Cancel");
                onFxThread(() -> {
                    dialog.button("cancelButton").fire();
                    assertFalse(dialog.warningStage.isShowing());
                    assertFalse(dialog.signingStage.isShowing());
                    return null;
                });
                var response = request.response.get(2, TimeUnit.SECONDS);
                assertEquals(204, response.statusCode());
                assertEquals("", response.body());
                verify(request.responder, times(1)).onDocumentSignFailed(isA(SigningCanceledByUserException.class));
                verify(dialog.autogram, never()).pickSigningKeyAndThen(any());
            } finally {
                onFxThread(() -> { dialog.close(); return null; });
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Warning.class)
    void continueKeepsJobOpenAndStartsKeySelection(Warning warning) throws Exception {
        try (var request = new PendingRequest()) {
            var dialog = onFxThread(() -> openDialog(warning, request.responder));
            try {
                onFxThread(() -> {
                    dialog.button("continueButton").fire();
                    assertFalse(dialog.warningStage.isShowing());
                    assertTrue(dialog.signingStage.isShowing());
                    return null;
                });
                verify(dialog.autogram).pickSigningKeyAndThen(any());
                verifyNoInteractions(request.responder);
                assertFalse(request.response.isDone());
            } finally {
                onFxThread(() -> { dialog.close(); return null; });
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Warning.class)
    void closingWarningKeepsJobOpenWithoutSigningOrCancelling(Warning warning) throws Exception {
        try (var request = new PendingRequest()) {
            var dialog = onFxThread(() -> openDialog(warning, request.responder));
            try {
                onFxThread(() -> {
                    Event.fireEvent(dialog.warningStage,
                            new WindowEvent(dialog.warningStage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    assertFalse(dialog.warningStage.isShowing());
                    assertTrue(dialog.signingStage.isShowing());
                    assertFalse(dialog.controller.mainButton.isDisabled());
                    return null;
                });
                verify(dialog.autogram, never()).pickSigningKeyAndThen(any());
                verifyNoInteractions(request.responder);
                assertFalse(request.response.isDone());
            } finally {
                onFxThread(() -> { dialog.close(); return null; });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Dialog openDialog(Warning warning, ServerResponder responder) throws Exception {
        var settings = new UserSettings();
        settings.setSignaturesValidity(true);
        var gui = new GUI(null, settings);
        var autogram = mock(Autogram.class);
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(StandardCharsets.UTF_8),
                "test.txt", MimeTypeEnum.TEXT), null);
        var parameters = SigningParametersResolver.buildRequested(SignatureProfile.BASELINE_B, SignatureForm.XAdES,
                DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                false, null, null, null, false, 640, false);
        var job = SigningJob.fromInput(SigningInput.fromDocument(document, parameters), responder);
        job.initializeVisualizations();
        gui.showSigningJob(job, autogram);
        var controllers = GUI.class.getDeclaredField("jobControllers");
        controllers.setAccessible(true);
        var controller = ((Map<SigningJob, SigningDialogController>) controllers.get(gui)).get(job);
        var signingStage = (Stage) controller.mainButton.getScene().getWindow();

        // Inject deterministic validation results; no trust-list downloads, certificates or card reader.
        try (var validator = mockStatic(SignatureValidator.class)) {
            validator.when(SignatureValidator::getInstance).thenReturn(mock(SignatureValidator.class));
            if (warning == Warning.INVALID) {
                var reports = mock(ValidationReports.class);
                when(reports.getSigningJob()).thenReturn(job);
                when(reports.haveSignatures()).thenReturn(true);
                when(reports.haveInvalidSignatures()).thenReturn(true);
                gui.onSignatureCheckCompleted(reports);
                gui.onSignatureValidationCompleted(reports);
            }
            controller.mainButton.fire();
        }
        var warningStage = Window.getWindows().stream()
                .filter(window -> window instanceof Stage stage && stage.getOwner() == signingStage)
                .map(Stage.class::cast).findFirst().orElseThrow();
        return new Dialog(signingStage, warningStage, controller, autogram);
    }

    private static <T> T onFxThread(Callable<T> action) throws Exception {
        var task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private record Dialog(Stage signingStage, Stage warningStage, SigningDialogController controller,
                          Autogram autogram) {
        Button button(String id) {
            return (Button) warningStage.getScene().lookup("#" + id);
        }

        void close() {
            warningStage.close();
            signingStage.close();
        }
    }

    private static class PendingRequest implements AutoCloseable {
        final HttpServer server;
        final HttpClient client = HttpClient.newHttpClient();
        final CompletableFuture<HttpResponse<String>> response;
        final ServerResponder responder;

        PendingRequest() throws Exception {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            var exchange = new CompletableFuture<HttpExchange>();
            server.createContext("/sign", exchange::complete);
            server.start();
            var uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/sign");
            response = client.sendAsync(HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            responder = spy(new ServerResponder(exchange.get(5, TimeUnit.SECONDS)));
        }

        @Override
        public void close() {
            response.cancel(true);
            client.shutdownNow();
            server.stop(0);
        }
    }
}
