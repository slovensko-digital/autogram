package digital.slovensko.autogram.core.errors;

public class OriginalDocumentNotFoundException extends AutogramException {

    public OriginalDocumentNotFoundException(String description) {
        super("Chyba ASiC-E kontajnera", "Súbor na podpis nebol nájdený", description);
    }
}
