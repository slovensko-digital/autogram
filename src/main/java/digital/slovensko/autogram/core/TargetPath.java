package digital.slovensko.autogram.core;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import digital.slovensko.autogram.core.errors.SourceAndTargetTypeMismatchException;
import digital.slovensko.autogram.core.errors.TargetAlreadyExistsException;
import digital.slovensko.autogram.core.errors.TargetDirectoryDoesNotExistException;
import digital.slovensko.autogram.core.errors.UnableToCreateDirectoryException;

public class TargetPath {
    private final Path targetDirectory;
    private final String targetName;
    private final Path sourceFile;
    private final boolean isForce;
    private final boolean isGenerated;
    private final boolean isForMultipleFiles;
    private final boolean isParents;

    private final FileSystem fs;

    public TargetPath(String target, Path source, boolean force, boolean parents, FileSystem fileSystem) {
        fs = fileSystem;
        sourceFile = source;
        isForce = force;
        isParents = parents;

        isGenerated = target == null;
        isForMultipleFiles = Files.isDirectory(source);
        if (isGenerated) {
            if (isForMultipleFiles) {

                targetDirectory = fs.getPath(
                        generateUniqueName(source.toAbsolutePath().getParent().toString(), source.getFileName().toString() + "_signed",
                                ""));
                targetName = null;

            } else {
                targetDirectory = source.toAbsolutePath().getParent();
                targetName = null;
            }

        } else {
            var targetFile = fs.getPath(target);
            if (!hasSourceAndTargetMatchingType(sourceFile, targetFile))
                throw new SourceAndTargetTypeMismatchException();

            if (Files.exists(targetFile) && !isForce)
                throw new TargetAlreadyExistsException();

            if (isForMultipleFiles) {
                targetDirectory = targetFile;
                targetName = null;

            } else {
                targetDirectory = targetFile.getParent();
                targetName = targetFile.getFileName().toString();
            }
        }
    }

    public static TargetPath fromParams(CliParameters params) {
        return new TargetPath(params.getTarget(), params.getSource().toPath(), params.isForce(),
                params.shouldMakeParentDirectories(), FileSystems.getDefault());
    }

    public static TargetPath fromSource(Path source) {
        return new TargetPath(null, source, false, false, FileSystems.getDefault());
    }

    /**
     * Create directory when we want to fill it out
     */
    public void mkdirIfDir() {
        if (targetDirectory == null || Files.exists(targetDirectory))
            return;

        if (isParents) {
            try {
                Files.createDirectories(targetDirectory);
            } catch (Exception e) {
                throw new UnableToCreateDirectoryException();
            }

            return;
        }

        if (!isForMultipleFiles)
            throw new TargetDirectoryDoesNotExistException();

        try {
            Files.createDirectory(targetDirectory);
        } catch (Exception e) {
            throw new UnableToCreateDirectoryException();
        }
    }

    private static boolean hasSourceAndTargetMatchingType(Path source, Path target) {
        if (!Files.exists(target))
            return true;

        var bothAreFiles = Files.isRegularFile(target) && Files.isRegularFile(source);
        var bothAreDirectories = Files.isDirectory(target) && Files.isDirectory(source);

        return (bothAreDirectories || bothAreFiles);
    }

    /*
     * Use these functions to get concrete file to be saved to
     *
     */

    public Path getSaveFilePath(Path singleSourceFile) {
        var file = _getSaveFilePath(singleSourceFile);

        if (Files.exists(file) && !isForce)
            throw new TargetAlreadyExistsException();

        return file;
    }

    private Path _getSaveFilePath(Path singleSourceFile) {
        var targetName = this.targetName == null ? generateTargetName(singleSourceFile) : this.targetName;
        var targetDirectoryPath = targetDirectory == null ? "" : targetDirectory.toString();
        Path targetSingleFile = fs.getPath(targetDirectoryPath, targetName);

        if (!Files.exists(targetSingleFile))
            return targetSingleFile;

        if (isForce)
            return targetSingleFile;

        if (isGenerated) {
            var parent = targetSingleFile.getParent();
            var baseName = com.google.common.io.Files
                    .getNameWithoutExtension(targetSingleFile.getFileName().toString());
            var extension = "."
                    + com.google.common.io.Files.getFileExtension(targetSingleFile.getFileName().toString());
            return fs.getPath(generateUniqueName(parent.toString(), baseName, extension));
        }

        throw new TargetAlreadyExistsException();
    }

    private String generateUniqueName(String parent, String baseName, String extension) {
        var count = 1;
        var newBaseName = baseName;
        parent = parent == null ? "" : parent;
        while (true) {
            var newTargetFile = _generateUniqueNameGetNewTargetFile(parent, newBaseName, extension);
            if (!Files.exists(newTargetFile))
                return newTargetFile.toString();

            if (count > 1000)
                throw new TargetAlreadyExistsException();

            newBaseName = baseName + " (" + count + ")";
            count++;
        }
    }

    private Path _generateUniqueNameGetNewTargetFile(String parent, String baseName, String extension) {
        return fs.getPath(parent, baseName + extension);
    }

    private String generateTargetName(Path singleSourceFile) {
        var extension = singleSourceFile.getFileName().toString().endsWith(".pdf") ? ".pdf" : ".asice";
        if (isGenerated || isForMultipleFiles)
            return com.google.common.io.Files.getNameWithoutExtension(singleSourceFile.getFileName().toString())
                    + "_signed"
                    + extension;

        else
            return com.google.common.io.Files.getNameWithoutExtension(targetDirectory.getFileName().toString())
                    + extension;
    }
}
