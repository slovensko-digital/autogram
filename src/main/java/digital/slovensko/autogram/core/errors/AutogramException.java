package digital.slovensko.autogram.core.errors;

import digital.slovensko.autogram.ui.gui.HasI18n;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;

import java.io.IOException;
import java.util.ResourceBundle;

import static digital.slovensko.autogram.core.errors.TsaServerMisconfiguredException.Error.MISSING_HOST_NAME;
import static digital.slovensko.autogram.core.errors.TsaServerMisconfiguredException.Error.REFUSED;

public class AutogramException extends RuntimeException {
    private final String errorCode;
    private final Object[] i18nArgs;

    private static final String SIGNING_CERTIFICATE_EXPIRED_EXCEPTION_MESSAGE_REGEX = ".*The signing certificate.*is expired.*";

    public AutogramException(String errorCode, Throwable e, Object... i18nArgs) {
        super(e);
        this.errorCode = errorCode;
        this.i18nArgs = i18nArgs;
    }

    public AutogramException(String errorCode, Object... i18nArgs) {
        this.errorCode = errorCode;
        this.i18nArgs = i18nArgs;
    }

    /**
     * Uses the class name as the error code.
     */
    protected AutogramException(Throwable e, Object... i18nArgs) {
        super(e);
        this.i18nArgs = i18nArgs;
        this.errorCode = this.getClass().getSimpleName();
    }

    /**
     * Uses the class name as the error code.
     */
    protected AutogramException(Object... i18nArgs) {
        this.i18nArgs = i18nArgs;
        this.errorCode = this.getClass().getSimpleName();
    }

    public String getHeading(ResourceBundle resources) {
        return HasI18n.translate(resources, "error.%s.heading".formatted(errorCode), i18nArgs);
    }

    public String getSubheading(ResourceBundle resources) {
        return HasI18n.translate(resources, "error.%s.subheading".formatted(errorCode), i18nArgs);
    }

    public String getDescription(ResourceBundle resources) {
        return HasI18n.translate(resources, "error.%s.description".formatted(errorCode), i18nArgs);
    }

    public static AutogramException createFromDSSException(DSSException e) {
        for (Throwable cause = e; cause != null && cause.getCause() != cause; cause = cause.getCause()) {
            if (cause.getMessage() != null) {
                if (cause instanceof java.security.ProviderException) {
                    if (cause.getMessage().contains("slotListIndex is 0 but token only has 0 slots"))
                        return new InitializationFailedException();

                    if (cause.getMessage().matches("slotListIndex is \\d+ but token only has \\d+ slots"))
                        return new SlotIndexOutOfRangeException(e);

                } else if (cause.getMessage().equals("CKR_FUNCTION_CANCELED")) {
                    return new FunctionCanceledException();
                } else if (cause.getMessage().equals("CKR_TOKEN_NOT_RECOGNIZED") || cause.getMessage().contains("no such algorithm: PKCS11 for provider")) {
                    return new TokenNotRecognizedException();
                } else if (cause.getMessage().equals("CKR_PIN_INCORRECT") || cause.getMessage().equals("CKR_FUNCTION_FAILED")) {
                    return new PINIncorrectException();
                } else if (cause.getMessage().equals("CKR_PIN_LOCKED")) {
                    return new PINLockedException();
                } else if (cause.getMessage().equals("Token has been removed")) {
                    return new TokenRemovedException();
                } else if (cause instanceof DSSExternalResourceException) {
                    return new TsaServerMisconfiguredException(REFUSED, cause);
                } else if (cause instanceof NullPointerException && cause.getMessage().contains("Host name")) {
                    return new TsaServerMisconfiguredException(MISSING_HOST_NAME, cause);
                } else if (cause instanceof IOException && (cause.getMessage().contains("The specified module could not be found") || cause.getMessage().contains("Zadaný modul sa nepodarilo"))) {
                    return new PkcsEidWindowsDllException(e);
                }
            }
        }

        return new UnrecognizedException(e);
    }

    public static AutogramException createFromIllegalArgumentException(IllegalArgumentException e) {
        for (Throwable cause = e; cause != null && cause.getCause() != cause; cause = cause.getCause()) {
            if (cause.getMessage() != null) {
                if (cause.getMessage().matches(SIGNING_CERTIFICATE_EXPIRED_EXCEPTION_MESSAGE_REGEX)) {
                    return new SigningWithExpiredCertificateException();
                }
            }
        }

        return new UnrecognizedException(e);
    }

    public boolean batchCanContinue() {
        return true;
    }
}
