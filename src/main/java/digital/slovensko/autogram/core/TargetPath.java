package digital.slovensko.autogram.core;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import digital.slovensko.autogram.core.errors.SourceAndTargetTypeMismatchException;
import digital.slovensko.autogram.core.errors.TargetAlreadyExistsException;
import digital.slovensko.autogram.core.errors.TargetDirectoryDoesNotExistException;
import digital.slovensko.autogram.core.errors.UnableToCreateDirectoryException;
import digital.slovensko.autogram.ui.cli.CliSettings;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;

public class TargetPath {
    private final Path targetDirectory;
    private final String targetName;
    private final Path sourceFile;
    private final boolean isForce;
    private final boolean isGenerated;
    private final boolean useUniqueFileName;
    private final boolean isForMultipleFiles;
    private final boolean isParents;
    private final boolean isSignatureLevelPades;
    private final FileSystem fs;

    public TargetPath(String target, Path source, boolean force, boolean parents, FileSystem fileSystem, boolean isSignatureLevelPades) {
        this(target, source, force, parents, Files.isDirectory(source), fileSystem, isSignatureLevelPades);
    }

    public TargetPath(String target, Path source, boolean force, boolean parents, boolean multipleFiles,
            FileSystem fileSystem, boolean isSignatureLevelPades) {
        fs = fileSystem;
        sourceFile = source;
        isForce = force;
        isParents = parents;
        this.isSignatureLevelPades = isSignatureLevelPades;

        isGenerated = target == null;
        var isTargetMissing = target == null;
        useUniqueFileName = isGenerated;
        isForMultipleFiles = multipleFiles;
        var useUniqueDirectoryName = isForMultipleFiles && !isTargetMissing && source == null;
        if (isTargetMissing) {
            if (isForMultipleFiles) {

                targetDirectory = fs.getPath(
                        generateUniqueName(source.toAbsolutePath().getParent().toString(),
                                source.getFileName().toString() + "_signed",
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

            if (Files.exists(targetFile) && !isForce) {
                if (isForMultipleFiles && useUniqueDirectoryName) {
                    targetFile = fs.getPath(
                            generateUniqueName(targetFile.toAbsolutePath().getParent().toString(),
                                    targetFile.getFileName().toString(),
                                    ""));
                } else {
                    throw new TargetAlreadyExistsException();
                }
            }

            if (isForMultipleFiles) {
                targetDirectory = targetFile;
                targetName = null;

            } else {
                targetDirectory = targetFile.getParent();
                targetName = targetFile.getFileName().toString();
            }
        }
    }

    public static TargetPath fromParams(CliSettings settings) {
        return new TargetPath(settings.getTarget(), settings.getSource().toPath(), settings.isForce(),
                settings.shouldMakeParentDirectories(), FileSystems.getDefault(), settings.shouldSignPDFAsPades());
    }

    public static TargetPath fromSource(Path source, boolean isSignatureLevelPades) {
        return new TargetPath(null, source, false, false, FileSystems.getDefault(), isSignatureLevelPades);
    }

    public static TargetPath fromTargetDirectory(Path targetDirectory, boolean isSignatureLevelPades) {
        return new TargetPath(targetDirectory.toString(), null, false, false, true,
                FileSystems.getDefault(), isSignatureLevelPades);
    }

    public Path getTargetDirectory() {
        return targetDirectory;
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

        if (source == null)
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
        return getSaveFilePath(singleSourceFile, shouldUsePdfExtension(singleSourceFile) ? ".pdf" : ".asice");

    }

    public Path getSaveFilePath(Path singleSourceFile, DSSDocument signedDocument) {
        return getSaveFilePath(singleSourceFile, getSignedFileExtension(singleSourceFile, signedDocument.getMimeType()));
    }

    private Path getSaveFilePath(Path singleSourceFile, String extension) {
        var file = _getSaveFilePath(singleSourceFile, extension);

        if (Files.exists(file) && !isForce)
            throw new TargetAlreadyExistsException();

        return file;
    }

    private Path _getSaveFilePath(Path singleSourceFile, String extension) {
        var targetName = this.targetName == null ? generateTargetName(singleSourceFile, extension) : this.targetName;
        var targetDirectoryPath = targetDirectory == null ? "" : targetDirectory.toString();
        Path targetSingleFile = fs.getPath(targetDirectoryPath, targetName);

        if (!Files.exists(targetSingleFile))
            return targetSingleFile;

        if (isForce)
            return targetSingleFile;

        if (useUniqueFileName) {
            var parent = targetSingleFile.getParent();
            var baseName = com.google.common.io.Files
                    .getNameWithoutExtension(targetSingleFile.getFileName().toString());
            var targetExtension = "."
                    + com.google.common.io.Files.getFileExtension(targetSingleFile.getFileName().toString());
            return fs.getPath(generateUniqueName(parent.toString(), baseName, targetExtension));
        }

        throw new TargetAlreadyExistsException();
    }

    private boolean shouldUsePdfExtension(Path singleSourceFile) {
        return singleSourceFile.getFileName().toString().toLowerCase().endsWith(".pdf") && isSignatureLevelPades;
    }

    /**
     * PAdES output is a PDF and container output is ASiC-E. Anything else is an already-signed
     * document re-signed in its own format (e.g. enveloping CAdES or XAdES without a container),
     * so it keeps the source file's extension.
     */
    private static String getSignedFileExtension(Path singleSourceFile, MimeType signedMimeType) {
        if (MimeTypeEnum.PDF.equals(signedMimeType))
            return ".pdf";

        if (MimeTypeEnum.ASICE.equals(signedMimeType))
            return ".asice";

        var sourceExtension = com.google.common.io.Files.getFileExtension(singleSourceFile.getFileName().toString());
        if (!sourceExtension.isEmpty())
            return "." + sourceExtension;

        if (MimeTypeEnum.PKCS7.equals(signedMimeType))
            return ".p7m";

        if (MimeTypeEnum.XML.equals(signedMimeType))
            return ".xml";

        return ".asice";
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

    private String generateTargetName(Path singleSourceFile, String extension) {
        if (useUniqueFileName || isForMultipleFiles)
            return com.google.common.io.Files.getNameWithoutExtension(singleSourceFile.getFileName().toString())
                    + "_signed"
                    + extension;

        else
            return com.google.common.io.Files.getNameWithoutExtension(targetDirectory.getFileName().toString())
                    + extension;
    }
}
