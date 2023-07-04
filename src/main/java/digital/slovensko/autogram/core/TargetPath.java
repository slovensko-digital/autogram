package digital.slovensko.autogram.core;

import java.io.File;
import java.nio.file.Paths;

import com.google.common.io.Files;

import digital.slovensko.autogram.core.errors.SourceAndTargetTypeMismatchException;
import digital.slovensko.autogram.core.errors.TargetAlreadyExistsException;
import digital.slovensko.autogram.core.errors.UnableToCreateDirectoryException;

public class TargetPath {
    private final File targetDirectory;
    private final String targetName;
    private final File sourceFile;
    private final boolean isForce;
    private final boolean isGenerated;
    private final boolean isForMultipleFiles;

    public TargetPath(String target, File source, boolean force) {
        sourceFile = source;
        isForce = force;

        isGenerated = target == null;
        isForMultipleFiles = source.isDirectory();
        if (isGenerated) {
            if (isForMultipleFiles) {

                targetDirectory = new File(generateUniqueName(source.getParent(), source.getName() + "_signed", ""));
                targetName = null;

            } else {
                targetDirectory = source.getParentFile();
                targetName = null;
            }

        } else {
            var targetFile = new File(target);
            if (!hasSourceAndTargetMatchingType(sourceFile, targetFile))
                throw new SourceAndTargetTypeMismatchException();

            if (targetFile.exists() && !isForce)
                throw new TargetAlreadyExistsException();

            if (isForMultipleFiles) {
                targetDirectory = targetFile;
                targetName = null;

            } else {
                targetDirectory = targetFile.getParentFile();
                targetName = targetFile.getName();
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
        if (!targetDirectory.exists())
            if (!targetDirectory.mkdir())
                throw new UnableToCreateDirectoryException();
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
            throw new TargetAlreadyExistsException();
        }
        return file;
    }

    private File _getSaveFilePath(File singleSourceFile) {
        var targetName = this.targetName == null ? generateTargetName(singleSourceFile) : this.targetName;

        var targetDirectoryPath = targetDirectory == null ? "" : targetDirectory.getPath();
        File targetSingleFile = Paths.get(targetDirectoryPath, targetName).toFile();
        if (!targetSingleFile.exists()) {
            return targetSingleFile;
        }

        if (isForce) {
            return targetSingleFile;
        }

        if (isGenerated) {
            var parent = targetSingleFile.getParent();
            var baseName = Files.getNameWithoutExtension(targetSingleFile.getName());
            var extension = "." + Files.getFileExtension(targetSingleFile.getName());
            return new File(generateUniqueName(parent, baseName, extension));
        }

        throw new TargetAlreadyExistsException();
    }

    private String generateUniqueName(String parent, String baseName, String extension) {
        var count = 1;
        var newBaseName = baseName;
        parent = parent == null ? "" : parent;
        while (true) {
            var newTargetFile = Paths.get(parent, newBaseName + extension).toFile();
            if (!newTargetFile.exists())
                return newTargetFile.getPath();

            if (count > 1000)
                throw new TargetAlreadyExistsException();

            newBaseName = baseName + " (" + count + ")";
            count++;
        }
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
