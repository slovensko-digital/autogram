package digital.slovensko.autogram.core;

import java.io.File;
import java.nio.file.Paths;

import com.google.common.io.Files;

import digital.slovensko.autogram.core.errors.AutogramException;

public class TargetPath {
    private final File targetFile;
    private final File sourceFile;
    private final boolean isForce;
    private final boolean isGenerated;

    public TargetPath(String target, File source, boolean force) {
        sourceFile = source;
        isForce = force;
        isGenerated = target == null;

        targetFile = isGenerated ? generateTargetPath(source) : new File(target);
    }

    private boolean isDirectory() {
        return sourceFile.isDirectory();
    }

    /**
     * Create directory when we want to fill it out, return success status
     * @return `true` if dir was created or it wasn't needed 
     */
    public boolean mkdirIfDir() {
        if (isDirectory()) {
            if (((targetFile.exists() && isForce) || !targetFile.exists())) {
                return targetFile.mkdir();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean hasSourceAndTargetMatchingType() {
        if (targetFile.exists()) {
            return ((targetFile.isDirectory() == sourceFile.isDirectory())
                    || (targetFile.isFile() == sourceFile.isFile()));
        }
        return isGenerated;
    }

    public boolean isTargetWriteable() {
        return isForce || isGenerated || !targetFile.exists();
    }

    private static File generateTargetPath(File source) {
        if (source.isDirectory()) {
            return new File(source.getPath() + "_signed");
        } else {
            var directory = source.getParent();
            var name = Files.getNameWithoutExtension(source.getName()) + "_signed";
            var extension = source.getName().endsWith(".pdf") ? ".pdf" : ".asice";
            return Paths.get(directory, name + extension).toFile();
        }
    }

    // SaveFileResponder

    public File getSaveFilePath(File singleSourceFile) {
        var targetName = generateTargetName(singleSourceFile);

        File targetSingleFile = Paths.get(targetFile.getPath(), targetName).toFile();
        if (!targetSingleFile.exists()) {
            return targetSingleFile;
        }

        if (isForce) {
            return targetSingleFile;
        }

        if (isGenerated) {
            var count = 1;
            var parent = targetSingleFile.getParent();
            var baseName = Files.getNameWithoutExtension(targetSingleFile.getName());
            var newBaseName = baseName;
            var extension = Files.getFileExtension(targetSingleFile.getName());
            while (true) {
                var newTargetFile = Paths.get(parent, newBaseName + "." + extension).toFile();
                if (!newTargetFile.exists())
                    return newTargetFile;

                newBaseName = baseName + " (" + count + ")";
                count++;
            }
        }

        throw new IllegalArgumentException(AutogramException.TARGET_ALREADY_EXISTS_EXCEPTION_MESSAGE);
    }

    private String generateTargetName(File singleSourceFile) {
        var extension = singleSourceFile.getName().endsWith(".pdf") ? ".pdf" : ".asice";
        if (isGenerated || sourceFile.isDirectory()) {
            return Files.getNameWithoutExtension(singleSourceFile.getName()) + "_signed" + extension;
        } else {
            return Files.getNameWithoutExtension(targetFile.getName()) + extension;
        }
    }
}
