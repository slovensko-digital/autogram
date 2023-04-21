package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class UnsupportedSignatureLevelExceptionError extends AutogramException {
    public UnsupportedSignatureLevelExceptionError(String signatureLevel) {
        super("Unsupported signature level", "Signature level " + signatureLevel + " is not supported",
                "Please use one of the following signature levels: XAdES_BASELINE_B, CAdES_BASELINE_B, PAdES_BASELINE_B");
    }
}
