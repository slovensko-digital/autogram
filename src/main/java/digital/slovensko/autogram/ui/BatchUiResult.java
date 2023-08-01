package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;

public class BatchUiResult {
    private final TargetPath targetPath;
    private final Map<File, File> targetFiles;
    private final Map<File, AutogramException> errors;

    public BatchUiResult(TargetPath targetPath, Map<File, File> targetFiles, Map<File, AutogramException> errors) {
        this.targetPath = targetPath;
        this.targetFiles = targetFiles;
        this.errors = errors;
    }

    public List<File> getTargetFilesSortedList() {
        return targetFiles.values().stream().filter(e -> e != null).sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
    }

    public Path getTargetDirectory() {
        return targetPath.getTargetDirectory();
    }

    public Stream<File> getFailedFilesList() {
        return errors.entrySet().stream().filter(e -> e.getValue() != null).map(e -> e.getKey())
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

    public Map<File, AutogramException> getErrorsMap() {
        return errors;
    }

    public boolean hasErrors() {
        return errors.values().stream().anyMatch(e -> e != null);
    }
}
