package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningBatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
