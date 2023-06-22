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
    private final String target;
    private final boolean overwrite;

    public SaveFileResponder(File file, Autogram autogram) {
        this(file, autogram, null, false);
    }

    public SaveFileResponder(File file, Autogram autogram, String target, boolean overwrite) {
        this.file = file;
        this.autogram = autogram;
        this.target = target;
        this.overwrite = overwrite;
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
        var directory = isValidTargetDirectory() ? target : file.getParent();
        var name = Files.getNameWithoutExtension(file.getName());

        var extension = ".asice";
        if (file.getName().endsWith(".pdf"))
            extension = ".pdf";

        var baseName = Paths.get(directory, name + "_signed").toString();
        var newBaseName = baseName;

        String pathName = isValidTargetFile() ? target : newBaseName + extension;

        if (overwrite) {
            return new File(pathName);
        } else {
            var count = 1;
            while(true) {
                var targetFile = new File(pathName);
                if(!targetFile.exists()) return targetFile;

                newBaseName = baseName + " (" + count + ")";
                count++;
            }
        }
    }

    private boolean isValidTargetDirectory() {
        return target != null && new File(target).isDirectory();
    }

    private boolean isValidTargetFile() {
        if (target == null)
            return false;

        File targetFile = new File(target);
        return targetFile.getParentFile().isDirectory() && !Files.getFileExtension(target).equals("");
    }
}
