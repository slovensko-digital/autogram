package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SigningCancellationTest {
    @Test
    void cancelSigningDeliversFailureThroughApplication() {
        var autogram = TestAutogramFactory.create();
        var job = mock(SigningJob.class);

        autogram.cancelSigning(job);

        verify(job).onJobCanceled();
    }

    @Test
    void visualizationDialogCancellationUsesApplicationAction() {
        var dialog = new AtomicReference<IgnorableException>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void showIgnorableExceptionDialog(IgnorableException exception) {
                dialog.set(exception);
            }
        };
        var settings = new UserSettings();
        settings.setCorrectDocumentDisplay(true);
        var autogram = new Autogram(ui, settings);
        var job = mock(SigningJob.class);
        when(job.getDocuments()).thenReturn(java.util.List.of());
        doThrow(new FailedVisualizationException(new IllegalStateException("preview failed")))
                .when(job).initializeVisualizations();
        autogram.startSigning(job);
        autogram.startVisualization(job);
        assertNotNull(dialog.get());

        dialog.get().getOnCancelCallback().run();

        verify(job).onJobCanceled();
    }
}
