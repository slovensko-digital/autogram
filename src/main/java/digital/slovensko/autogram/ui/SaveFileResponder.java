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
        var directory = getDirectory();
        var name = getName();
        var extension = getExtension();

        var baseName = Paths.get(directory, name).toString();
        var newBaseName = baseName;

        if (overwrite) {
            return new File(newBaseName + extension);
        } else {
            var count = 1;
            while(true) {
                var targetFile = new File(newBaseName + extension);
                if(!targetFile.exists()) return targetFile;
                newBaseName = baseName + " (" + count + ")";
                count++;
            }
        }
    }

    private String getDirectory() {
        if (target == null) {
            return file.getParent();
        } else if (isFile(target)) {
            return new File(target).getParent();
        } else {
            return target;
        }
    }

    private String getName() {
        if (target == null || !isFile(target)) {
            return Files.getNameWithoutExtension(file.getName()) + "_signed";
        } else {
            return Files.getNameWithoutExtension(target);
        }
    }

    private String getExtension() {
        if (target == null || !isFile(target)) {
            return file.getName().endsWith(".pdf") ? ".pdf" : ".asice";
        } else {
            return "." + Files.getFileExtension(target);
        }
    }

    private boolean isFile(String file) {
        return !Files.getFileExtension(file).equals("");
    }
}
