package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import digital.slovensko.autogram.core.errors.PdfPasswordProtectedException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchUiResultTest {
    private static final TargetPath TARGET = TargetPath.fromTargetDirectory(Path.of("signed"), false);

    @Test
    void skippedFilesAreNotErrors() {
        var signed = new File("signed.pdf");
        var skipped = new File("skipped.pdf");
        var canceled = new File("canceled.pdf");
        var targets = new HashMap<File, File>();
        var errors = new HashMap<File, AutogramException>();
        targets.put(signed, new File("signed/signed_signed.pdf"));
        errors.put(signed, null);
        targets.put(skipped, null);
        errors.put(skipped, new SigningCanceledByUserException());
        targets.put(canceled, null);
        errors.put(canceled, new PasswordNotProvidedException());

        var result = new BatchUiResult(TARGET, targets, errors);

        assertFalse(result.hasErrors());
        assertEquals(2, result.getSkippedCount());
        assertEquals(1, result.getSignedCount());
        assertEquals(List.of(), result.getFailedFilesList().toList());
    }

    @Test
    void failedFilesAreErrorsAlongsideSkippedOnes() {
        var locked = new File("locked.pdf");
        var skipped = new File("skipped.pdf");
        var targets = new HashMap<File, File>();
        var errors = new HashMap<File, AutogramException>();
        targets.put(locked, null);
        errors.put(locked, new PdfPasswordProtectedException());
        targets.put(skipped, null);
        errors.put(skipped, new SigningCanceledByUserException());

        var result = new BatchUiResult(TARGET, targets, errors);

        assertTrue(result.hasErrors());
        assertEquals(List.of(locked), result.getFailedFilesList().toList());
        assertEquals(1, result.getSkippedCount());
        assertEquals(0, result.getSignedCount());
    }
}
