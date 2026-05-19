package digital.slovensko.autogram.core.errors;

import org.checkerframework.checker.nullness.qual.NonNull;

public class SourceDoesNotExistException extends AutogramException {
    public SourceDoesNotExistException() {
        this("");
    }

    public SourceDoesNotExistException(@NonNull String sourcePath) {
        super(new Object[]{sourcePath.isBlank() ? "" : (" \"" + sourcePath + "\"")});
    }
}
