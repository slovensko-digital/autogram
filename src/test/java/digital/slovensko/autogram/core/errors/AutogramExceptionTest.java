package digital.slovensko.autogram.core.errors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AutogramExceptionTest {

    @Test
    void createFromIllegalArgumentExceptionNoMessageTest() {
        Assertions.assertSame(UnrecognizedException.class, AutogramException.createFromIllegalArgumentException(new IllegalArgumentException()).getClass());
    }

    @Test
    void createFromIllegalArgumentExceptionEmptyMessageTest() {
        Assertions.assertSame(UnrecognizedException.class, AutogramException.createFromIllegalArgumentException(new IllegalArgumentException("")).getClass());
    }

    @Test
    void createFromIllegalArgumentExceptionUnknownMessageTest() {
        Assertions.assertSame(UnrecognizedException.class, AutogramException.createFromIllegalArgumentException(new IllegalArgumentException("unknown-message")).getClass());
    }

    @Test
    void createFromIllegalArgumentExceptionSigningCertificateExpiredMessageTest() {
        Assertions.assertSame(SigningWithExpiredCertificateException.class, AutogramException.createFromIllegalArgumentException(new IllegalArgumentException("The signing certificate (notBefore : 2022-12-29T08:34:56Z, notAfter : 2022-12-30T08:34:56Z) is expired at signing time 2023-05-25T07:35:18Z! Change signing certificate or use method setSignWithExpiredCertificate(true).")).getClass());
    }
}