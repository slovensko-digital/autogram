package digital.slovensko.autogram.core;

import java.io.File;
import java.nio.file.Paths;

import com.google.common.io.Files;

import digital.slovensko.autogram.core.errors.AutogramException;

public class TargetPath {
    private final File targetDirectory;
    private final String targetName;
    private final File sourceFile;
    private final boolean isForce;
    private final boolean isGenerated;
    private final boolean isForMultipleFiles;

    public TargetPath(String target, File source, boolean force) {
        this.sourceFile = source;
        this.isForce = force;

        this.isGenerated = target == null;
        this.isForMultipleFiles = source.isDirectory();
        if (isGenerated) {
            this.targetDirectory = isForMultipleFiles ? new File(source.getPath() + "_signed")
                    : source.getParentFile();
            this.targetName = null;
        } else {
            var targetFile = new File(target);
            if (!hasSourceAndTargetMatchingType(sourceFile, targetFile)) {
                throw new IllegalArgumentException("Source and target incompatible file types");
            }
            if (source.isDirectory()) {
                this.targetDirectory = targetFile;
                this.targetName = null;
            } else {
                this.targetDirectory = targetFile.getParentFile();
                this.targetName = targetFile.getName();
            }
        }
    }

    public static TargetPath fromParams(CliParameters params) {
        return new TargetPath(params.getTarget(), params.getSource(), params.isForce());
    }

    public static TargetPath fromSource(File source) {
        return new TargetPath(null, source, false);
    }

    /**
     * Create directory when we want to fill it out
     */
    public void mkdirIfDir() {
        if (isForMultipleFiles && !targetDirectory.exists()) {
            if (!targetDirectory.mkdir()) {
                throw new IllegalArgumentException("Unable to create target directory");
            }
        }
    }

    private static boolean hasSourceAndTargetMatchingType(File source, File target) {
        if (target.exists()) {
            var bothAreFiles = target.isFile() && source.isFile();
            var bothAreDirectories = target.isDirectory() && source.isDirectory();
            return (bothAreDirectories || bothAreFiles);
        }
        return true;
    }

    /*
     * Use these functions to get concrete file to be saved to
     *
     */

    public File getSaveFilePath(File singleSourceFile) {

        var file = _getSaveFilePath(singleSourceFile);

        if (file.exists() && !isForce) {
            throw new IllegalArgumentException(AutogramException.TARGET_ALREADY_EXISTS_EXCEPTION_MESSAGE);
        }
        return file;
    }

    private File _getSaveFilePath(File singleSourceFile) {
        var targetName = this.targetName == null ? generateTargetName(singleSourceFile) : this.targetName;

        File targetSingleFile = Paths.get(targetDirectory.getPath(), targetName).toFile();
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

                if (count > 1000)
                    throw new IllegalArgumentException(AutogramException.TARGET_ALREADY_EXISTS_EXCEPTION_MESSAGE);

                newBaseName = baseName + " (" + count + ")";
                count++;
            }
        }

        throw new IllegalArgumentException(AutogramException.TARGET_ALREADY_EXISTS_EXCEPTION_MESSAGE);
    }

    private String generateTargetName(File singleSourceFile) {
        var extension = singleSourceFile.getName().endsWith(".pdf") ? ".pdf" : ".asice";
        if (isGenerated || isForMultipleFiles) {
            return Files.getNameWithoutExtension(singleSourceFile.getName()) + "_signed" + extension;
        } else {
            return Files.getNameWithoutExtension(targetDirectory.getName()) + extension;
        }
    }
}
