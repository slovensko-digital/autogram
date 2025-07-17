package digital.slovensko.autogram.core.errors;

public class EmptyDirectorySelectedException extends AutogramException {
    public EmptyDirectorySelectedException(String filePath) {
        super(new Object[]{filePath});
    }
}
