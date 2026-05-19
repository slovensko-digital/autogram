package digital.slovensko.autogram.core.errors;

public class PDFSignatureLevelIsNotValidException extends AutogramException {

    public PDFSignatureLevelIsNotValidException(String signatureLevelString) {
        super(new Object[]{signatureLevelString});
    }
}
