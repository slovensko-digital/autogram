package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningResponder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

import java.io.File;
import java.io.IOException;

public class SaveFileResponder implements SigningResponder {
    private final File file;
    private final Autogram autogram;
    private final TargetPath targetPathBuilder;

    public SaveFileResponder(File file, Autogram autogram, boolean isSignatureLevelPades) {
        this(file, autogram, TargetPath.fromSource(file.toPath(), isSignatureLevelPades));
    }

    public SaveFileResponder(File file, Autogram autogram, TargetPath targetPathBuilder) {
        this.file = file;
        this.autogram = autogram;
        this.targetPathBuilder = targetPathBuilder;
    }

    @Override
    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = targetPathBuilder.getSaveFilePath(file.toPath(), MimeTypeEnum.PDF.equals(signedDocument.getDocument().getMimeType()));
            signedDocument.getDocument().save(targetFile.toString());
            autogram.onDocumentSaved(targetFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDocumentFailed(AutogramException error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
