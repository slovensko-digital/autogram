package digital.slovensko.autogram.core;

import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.asic.common.SecureContainerHandlerBuilder;
import eu.europa.esig.dss.asic.common.ZipUtils;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.pdf.PdfMemoryUsageSetting;
import eu.europa.esig.dss.pdf.ServiceLoaderPdfObjFactory;
import eu.europa.esig.dss.signature.resources.TempFileResourcesHandlerBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Lets DSS build signed documents in temporary files instead of the heap, so the memory needed for signing does not
 * grow with the size of the document.
 * <p>
 * DSS deletes its temporary files only when the application exits. Each signing therefore gets its own directory:
 * {@link #keepOnly} deletes the intermediate files once the document is signed, {@link #delete} everything when
 * the signing fails, and {@link #deleteIfTemporary} the signed document once it is handed over.
 */
public class SigningResources {
    private static final String TEMP_PREFIX = "autogram-signing-";

    private static volatile boolean enabled;

    private Path directory;

    /**
     * Turned on at startup by the --temp-files option, without it documents are signed in memory as before.
     * ZIP containers (ASiC) are built by a DSS singleton, so this is set once, for all of them.
     */
    public static void useTemporaryFiles() {
        ZipUtils.getInstance().setZipContainerHandlerBuilder(
                new SecureContainerHandlerBuilder().setResourcesHandlerBuilder(temporaryFilesIn(null)));
        enabled = true;
    }

    /** Back to the default in-memory signing. */
    static void useMemory() {
        ZipUtils.getInstance().setZipContainerHandlerBuilder(new SecureContainerHandlerBuilder());
        enabled = false;
    }

    static boolean isEnabled() {
        return enabled;
    }

    /** The same PDF implementation DSS picks by default, reading and writing the PDF through temporary files. */
    IPdfObjFactory pdfObjFactory() {
        var factory = new ServiceLoaderPdfObjFactory();
        factory.setPdfMemoryUsageSetting(PdfMemoryUsageSetting.fileOnly());
        factory.setResourcesHandlerBuilder(temporaryFilesIn(getDirectory().toFile()));
        return factory;
    }

    /** Deletes the intermediate files of this signing, only the signed document stays until it is handed over. */
    void keepOnly(DSSDocument signedDocument) {
        if (directory == null)
            return;

        var kept = signedDocument instanceof FileDocument fileDocument ? fileDocument.getFile().toPath() : null;
        try (var files = Files.list(directory)) {
            files.filter(file -> !file.equals(kept)).forEach(SigningResources::deleteQuietly);
        } catch (IOException e) {
            Logging.log("Unable to clean temporary directory " + directory + ": " + e.getMessage());
        }

        if (kept == null || !directory.equals(kept.getParent()))
            deleteQuietly(directory);
    }

    /** Deletes this signing's directory with everything in it, the signing failed. */
    void delete() {
        if (directory != null)
            deleteDirectory(directory);
    }

    public static boolean isTemporary(DSSDocument document) {
        return document instanceof FileDocument fileDocument
                && fileDocument.getFile().getName().startsWith(TEMP_PREFIX);
    }

    /** Deletes the signed document once it is handed over, together with its signing's directory. */
    public static void deleteIfTemporary(DSSDocument document) {
        if (!isTemporary(document))
            return;

        var file = ((FileDocument) document).getFile().toPath();
        deleteQuietly(file);

        var parent = file.getParent();
        if (parent != null && parent.getFileName().toString().startsWith(TEMP_PREFIX))
            deleteDirectory(parent);
    }

    private Path getDirectory() {
        if (directory == null) {
            try {
                directory = Files.createTempDirectory(TEMP_PREFIX);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to create a temporary directory for signing", e);
            }
            // registered before the files DSS creates in it, so on exit they are deleted first
            directory.toFile().deleteOnExit();
        }

        return directory;
    }

    private static TempFileResourcesHandlerBuilder temporaryFilesIn(File directory) {
        var builder = new TempFileResourcesHandlerBuilder();
        builder.setFileNamePrefix(TEMP_PREFIX);
        if (directory != null)
            builder.setTempFileDirectory(directory);

        return builder;
    }

    private static void deleteDirectory(Path directory) {
        if (!Files.isDirectory(directory))
            return;

        try (Stream<Path> files = Files.list(directory)) {
            files.forEach(SigningResources::deleteQuietly);
        } catch (IOException e) {
            Logging.log("Unable to clean temporary directory " + directory + ": " + e.getMessage());
        }
        deleteQuietly(directory);
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // it is still deleted when the application exits
            Logging.log("Unable to delete temporary file " + path + ": " + e.getMessage());
        }
    }
}
