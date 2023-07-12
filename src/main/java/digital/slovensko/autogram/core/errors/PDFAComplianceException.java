package digital.slovensko.autogram.core.errors;

public class PDFAComplianceException extends AutogramException {
    public PDFAComplianceException() {
        super("Nastala chyba", "Dokument nie je vo formáte PDF/A", "Dokument, ktorý ste chceli podpísať je vo formáte, ktorý úrady nemusia akceptovať.");
    }
}
