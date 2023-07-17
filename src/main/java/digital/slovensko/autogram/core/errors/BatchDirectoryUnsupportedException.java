package digital.slovensko.autogram.core.errors;

public class BatchDirectoryUnsupportedException extends AutogramException {
    public BatchDirectoryUnsupportedException() {
        super("Priečinok nevieme podpísať", "Priečinky musíte podpísať samostatne", "Hromadné podpisovanie priečinkov zatiaľ nepodporujeme.");
    }
}