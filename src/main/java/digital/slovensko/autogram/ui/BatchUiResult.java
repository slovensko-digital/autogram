package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;

public class BatchUiResult {
    private final TargetPath targetPath;
    private final Map<File, File> targetFiles;
    private final Map<File, AutogramException> errors;

    public BatchUiResult(TargetPath targetPath, Map<File, File> targetFiles, Map<File, AutogramException> errors) {
        this.targetPath = targetPath;
        this.targetFiles = targetFiles;
        this.errors = errors;
    }

    /** Not signed because the user skipped it or canceled signing, nothing went wrong. */
    public static boolean isSkipped(AutogramException error) {
        return error instanceof SigningCanceledByUserException || error instanceof PasswordNotProvidedException;
    }

    public List<File> getTargetFilesSortedList() {
        return targetFiles.values().stream().filter(e -> e != null).sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
    }

    public int getSignedCount() {
        return getTargetFilesSortedList().size();
    }

    public Path getTargetDirectory() {
        return targetPath.getTargetDirectory();
    }

    /** Files that failed, without the skipped ones. */
    public Stream<File> getFailedFilesList() {
        return errors.entrySet().stream().filter(e -> e.getValue() != null && !isSkipped(e.getValue()))
                .map(e -> e.getKey())
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

    public long getSkippedCount() {
        return errors.values().stream().filter(e -> e != null && isSkipped(e)).count();
    }

    public Map<File, AutogramException> getErrorsMap() {
        return errors;
    }

    /** True if some file failed, skipped files don't count. */
    public boolean hasErrors() {
        return errors.values().stream().anyMatch(e -> e != null && !isSkipped(e));
    }
}
