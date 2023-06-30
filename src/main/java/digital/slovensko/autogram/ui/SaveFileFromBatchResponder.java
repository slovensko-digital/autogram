package digital.slovensko.autogram.ui;

import com.google.common.io.Files;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class SaveFileFromBatchResponder extends Responder {
    private final File file;
    private final Autogram autogram;
    private final File targetDirectory;
    private final Consumer<File> callback;

    public SaveFileFromBatchResponder(File file, Autogram autogram, File targetDirectory, Consumer<File> callback) {
        this.file = file;
        this.autogram = autogram;
        this.targetDirectory = targetDirectory;
        this.callback = callback;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = getTargetFile();
            signedDocument.getDocument().save(targetFile.getPath());
            Logging.log("Saved file " + targetFile.toString());
            autogram.updateBatch();
            callback.accept(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(AutogramException error) {
        // TODO tu je zozrany error
        System.err.println("Sign failed error occurred: " + error.toString());
    }

    private File getTargetFile() {
        var directory = targetDirectory.getPath();
        var name = Files.getNameWithoutExtension(file.getName());

        var extension = ".asice";
        if (file.getName().endsWith(".pdf"))
            extension = ".pdf";

        var baseName = Paths.get(directory, name + "_signed").toString();
        var newBaseName = baseName;

        var count = 1;
        while (true) {
            var targetFile = new File(newBaseName + extension);
            if (!targetFile.exists())
                return targetFile;

            newBaseName = baseName + " (" + count + ")";
            count++;
        }
    }
}
