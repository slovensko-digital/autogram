package digital.slovensko.autogram.core.errors;

public class BatchExpiredException extends AutogramException {
    public BatchExpiredException() {
        super("Hromadné podpisovanie bolo ukončené", "Čas na podpisovanie vypršal", "Skúste zopakovať akciu od začiatku.");
    }
}
