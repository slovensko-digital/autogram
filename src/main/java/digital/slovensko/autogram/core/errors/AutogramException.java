package digital.slovensko.autogram.core.errors;

import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;

public class AutogramException extends RuntimeException {
    private final String heading;
    private final String subheading;
    private final String description;

    private static final String SIGNING_CERTIFICATE_EXPIRED_EXCEPTION_MESSAGE_REGEX = ".*The signing certificate.*is expired.*";

    public AutogramException(String heading, String subheading, String description, Throwable e) {
        super(e);
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public AutogramException(String heading, String subheading, String description) {
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public String getSubheading() {
        return subheading;
    }

    public String getDescription() {
        return description;
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
                    return new TsaServerMisconfiguredException("Nastavený TSA server odmietol pridať časovú pečiatku. Skontrolujte nastavenia TSA servera.", cause);
                } else if (cause instanceof NullPointerException && cause.getMessage().contains("Host name")) {
                    return new TsaServerMisconfiguredException("Nie je nastavená žiadna adresa TSA servera. Skontrolujte nastavenia TSA servera.", cause);
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
