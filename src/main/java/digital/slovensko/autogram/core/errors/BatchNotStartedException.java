package digital.slovensko.autogram.core.errors;

public class BatchNotStartedException extends AutogramException {
    public BatchNotStartedException() {
        super("Hromadné podpisovanie nebolo začaté", "Začnite hromadné podpisovanie pred samotným podpisovaním", "Hromadné podpisovanie je potrebné začať pred podpisovaním dokumentov.");
    }
}
