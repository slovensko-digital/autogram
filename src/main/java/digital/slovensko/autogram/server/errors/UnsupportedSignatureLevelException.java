package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class UnsupportedSignatureLevelException extends AutogramException {
    public UnsupportedSignatureLevelException(String signatureLevel) {
        super(new Object[]{signatureLevel});
    }
}
