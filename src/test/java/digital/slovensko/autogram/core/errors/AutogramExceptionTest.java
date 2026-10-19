package digital.slovensko.autogram.core.errors;

import eu.europa.esig.dss.model.DSSException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.ProviderException;

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
    void createFromDSSExceptionTokenRemovedTest() {
        // signing after the card was removed from the reader
        var e = new DSSException("Unable to sign : Token has been removed", new ProviderException("Token has been removed"));

        Assertions.assertSame(TokenRemovedException.class, AutogramException.createFromDSSException(e).getClass());
    }

    @Test
    void createFromDSSExceptionTokenNotRecognizedKeepsCauseTest() {
        var e = new DSSException("Unable to sign", new IllegalStateException("CKR_TOKEN_NOT_RECOGNIZED"));

        var result = AutogramException.createFromDSSException(e);
        Assertions.assertSame(TokenNotRecognizedException.class, result.getClass());
        // shown in error details
        Assertions.assertSame(e, result.getCause());
    }

    @Test
    void createFromDSSExceptionCanceledPasswordTest() {
        // SunPKCS11 wraps what the password callback throws
        var canceled = new PasswordNotProvidedException();
        var login = new javax.security.auth.login.LoginException("Unable to perform password callback");
        login.initCause(canceled);
        var e = new DSSException("Can't initialize Sun PKCS#11 security provider", new java.io.IOException("load failed", login));

        Assertions.assertSame(canceled, AutogramException.createFromDSSException(e));
    }

    @Test
    void createFromDSSExceptionSlotIndexTest() {
        var noSlots = new DSSException("Unable to instantiate", new ProviderException("slotListIndex is 0 but token only has 0 slots"));
        var outOfRange = new DSSException("Unable to instantiate", new ProviderException("slotListIndex is 3 but token only has 2 slots"));

        Assertions.assertSame(InitializationFailedException.class, AutogramException.createFromDSSException(noSlots).getClass());
        Assertions.assertSame(SlotIndexOutOfRangeException.class, AutogramException.createFromDSSException(outOfRange).getClass());
    }

    @Test
    void createFromIllegalArgumentExceptionSigningCertificateExpiredMessageTest() {
        Assertions.assertSame(SigningWithExpiredCertificateException.class, AutogramException.createFromIllegalArgumentException(new IllegalArgumentException("The signing certificate (notBefore : 2022-12-29T08:34:56Z, notAfter : 2022-12-30T08:34:56Z) is expired at signing time 2023-05-25T07:35:18Z! Change signing certificate or use method setSignWithExpiredCertificate(true).")).getClass());
    }
}
