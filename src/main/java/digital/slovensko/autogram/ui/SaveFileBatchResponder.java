package digital.slovensko.autogram.ui;

import com.google.common.io.Files;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SaveFileBatchResponder extends Responder {
    private final File file;
    private final Autogram autogram;
    private final File targetDirectory;

    public SaveFileBatchResponder(File file, Autogram autogram, File targetDirectory) {
        this.file = file;
        this.autogram = autogram;
        this.targetDirectory = targetDirectory;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = getTargetFile();
            signedDocument.getDocument().save(targetFile.getPath());
            // TODO co s tymto???? bud by mal sa ukazat jeden alebo viacero podla toho ci ide o batch alebo nie
            // autogram.onDocumentSaved(List.of(targetFile));
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
