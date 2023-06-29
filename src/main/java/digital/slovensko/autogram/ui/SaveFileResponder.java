package digital.slovensko.autogram.ui;

import com.google.common.io.Files;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class SaveFileResponder extends Responder {
    private final File file;
    private final Autogram autogram;
    private final String targetPath;
    private final String targetName;
    private final boolean overwrite;
    private final boolean isTargetGenerated;

    public SaveFileResponder(File file, Autogram autogram) {
        this(file, autogram, null, null, false, false);
    }

    public SaveFileResponder(File file, Autogram autogram, String targetPath, String targetName, boolean overwrite, boolean isTargetGenerated) {
        this.file = file;
        this.autogram = autogram;
        this.targetPath = targetPath;
        this.targetName = targetName;
        this.overwrite = overwrite;
        this.isTargetGenerated = isTargetGenerated;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = getTargetFile();
            signedDocument.getDocument().save(targetFile.getPath());
            autogram.onDocumentSaved(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(AutogramException error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }

    private File getTargetFile() {
        var targetName = generateTargetName();
        File targetFile = Paths.get(targetPath, targetName).toFile();
        if (!targetFile.exists()) {
           return targetFile;
        }

        if (overwrite) {
            return targetFile;
        }

        if (isTargetGenerated) {
            var count = 1;
            var parent = targetFile.getParent();
            var baseName = Files.getNameWithoutExtension(targetFile.getName());
            var newBaseName = baseName;
            var extension = Files.getFileExtension(targetFile.getName());
            while(true) {
                var newTargetFile = Paths.get(parent, newBaseName + "." +extension).toFile();
                if(!newTargetFile.exists()) return newTargetFile;

                newBaseName = baseName + " (" + count + ")";
                count++;
            }
        }

        throw new IllegalArgumentException(AutogramException.TARGET_ALREADY_EXISTS_EXCEPTION_MESSAGE);
    }

    private String generateTargetName() {
        var extension = file.getName().endsWith(".pdf") ? ".pdf" : ".asice";
        return targetName == null ? Files.getNameWithoutExtension(file.getName()) + "_signed" + extension : Files.getNameWithoutExtension(targetName) + extension;
    }
}
