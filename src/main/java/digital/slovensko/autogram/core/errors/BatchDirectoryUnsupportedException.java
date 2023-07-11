package digital.slovensko.autogram.core.errors;

public class BatchDirectoryUnsupportedException extends AutogramException {
    public BatchDirectoryUnsupportedException() {
        super("Priečinok nevieme podpísať", "", "Hromadné podpisovanie priečinkov zatiaľ nepodporujeme.");
    }
}