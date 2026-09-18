package digital.slovensko.autogram.core.errors;

public class SourceDoesNotExistException extends AutogramException {
    public SourceDoesNotExistException() {
        this("");
    }

    public SourceDoesNotExistException(String sourcePath) {
        super(new Object[]{(sourcePath == null || sourcePath.isBlank()) ? "" : (" \"" + sourcePath + "\"")});
    }
}
