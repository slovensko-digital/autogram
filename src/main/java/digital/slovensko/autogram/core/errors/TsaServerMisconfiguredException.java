package digital.slovensko.autogram.core.errors;

public class TsaServerMisconfiguredException extends AutogramException {
    public TsaServerMisconfiguredException(String description, Throwable e) {
        super("Chyba TSA servera", "Nepodarilo sa pridať časovú pečiatku", description, e);
    }
}
