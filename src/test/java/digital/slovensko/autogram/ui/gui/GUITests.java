package digital.slovensko.autogram.ui.gui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.SupportedLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GUITests {

    @Test
    void batchPositionIsShownInTitle() {
        var job = mock(SigningJob.class);
        when(job.getName()).thenReturn("a.txt");
        when(job.getBatch()).thenReturn(new Batch(5));
        when(job.getBatchPosition()).thenReturn(2);

        Assertions.assertEquals("Dokument a.txt (2 z 5)",
                GUI.buildTitle(job, SupportedLanguage.SLOVAK.loadResources()));
        Assertions.assertEquals("Document a.txt (2 of 5)",
                GUI.buildTitle(job, SupportedLanguage.ENGLISH.loadResources()));
    }

    @Test
    void titleHasNoPositionWithoutBatch() {
        var job = mock(SigningJob.class);
        when(job.getName()).thenReturn("a.txt");

        Assertions.assertEquals("Dokument a.txt", GUI.buildTitle(job, SupportedLanguage.SLOVAK.loadResources()));
    }

    @Test
    void multiDocumentTitleIsUnchanged() {
        var job = mock(SigningJob.class);
        when(job.isMultiDocument()).thenReturn(true);
        when(job.getPreviewDocumentsCount()).thenReturn(3);

        Assertions.assertEquals("Dokumenty (3)", GUI.buildTitle(job, SupportedLanguage.SLOVAK.loadResources()));
    }
}
