package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningBatch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.ValidationReports;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BatchCallbackControllerTest {
    @Test
    void cancelButtonOnlyCallsApplicationAction() {
        var batch = new SigningBatch(2);
        var autogram = mock(Autogram.class);
        var gui = mock(GUI.class);
        var controller = new BatchDialogController(batch, autogram, gui);

        controller.onCancelBatchButtonPressed(null);

        verify(autogram).cancelBatch(batch);
        verifyNoInteractions(gui);
        assertFalse(batch.isEnded());
    }

    @Test
    void closingBatchUiDoesNotChangeDomainState() {
        var batch = new SigningBatch(2);
        batch.start(null);

        new GUI(null, null).closeBatch();

        assertFalse(batch.isEnded());
    }

    @Test
    void closingInteractiveSigningWindowSkipsRemainingBatchDocuments() throws ReflectiveOperationException {
        var batch = mock(Batch.class);
        when(batch.isPresent()).thenReturn(true);
        when(batch.isInteractive()).thenReturn(true);
        var job = mock(SigningJob.class);
        when(job.isPartOfBatch()).thenReturn(true);
        when(job.getBatch()).thenReturn(batch);
        var autogram = mock(Autogram.class);
        var controller = mock(SigningDialogController.class);
        var gui = new GUI(null, null);
        setField(gui, "autogram", autogram);
        @SuppressWarnings("unchecked")
        var controllers = (Map<SigningJob, SigningDialogController>) getField(gui, "jobControllers");
        controllers.put(job, controller);

        gui.cancelJob(job);

        verify(autogram).skipRemainingDocuments(job);
        verify(autogram, never()).cancelSigning(any());
        verify(controller, never()).close();
    }

    @Test
    void lateResultsForClosedSigningWindowAreIgnored() throws ReflectiveOperationException {
        var job = mock(SigningJob.class);
        var controller = mock(SigningDialogController.class);
        var gui = new GUI(null, null);
        @SuppressWarnings("unchecked")
        var controllers = (Map<SigningJob, SigningDialogController>) getField(gui, "jobControllers");
        controllers.put(job, controller);
        gui.onSigningSuccess(job);
        var reports = mock(ValidationReports.class);
        when(reports.getSigningJob()).thenReturn(job);

        assertDoesNotThrow(() -> gui.onSignatureCheckCompleted(reports));
        assertDoesNotThrow(() -> gui.onSignatureValidationCompleted(reports));
        assertDoesNotThrow(() -> gui.onPDFAComplianceCheckFailed(job));

        verify(controller).close();
        verify(controller, never()).onSignatureCheckCompleted(any());
        verify(controller, never()).onSignatureValidationCompleted(any());
    }

    @Test
    void documentsOutsideBatchAreBlockedUntilItEnds() {
        var settings = new UserSettings();
        settings.setBulkEnabled(true); // skips the mode dialog
        var gui = new GUI(null, settings);
        var batch = new SigningBatch(2);
        var single = mock(SigningJob.class);
        var inBatch = mock(SigningJob.class);
        when(inBatch.isPartOfBatch()).thenReturn(true);
        assertFalse(gui.isSigningBlockedByBatch(single));

        gui.selectBatchMode(batch, mode -> {}, () -> {});

        assertTrue(gui.isSigningBlockedByBatch(single));
        assertFalse(gui.isSigningBlockedByBatch(inBatch));

        batch.end();

        assertFalse(gui.isSigningBlockedByBatch(single));
    }

    private static Object getField(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
