package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SigningCertificateExpiredMessagePredicateTest {

    private SigningCertificateExpiredMessagePredicate signingWithExpiredCertificateExceptionPredicate;

    @BeforeEach
    void setUp() {
        this.signingWithExpiredCertificateExceptionPredicate = new SigningCertificateExpiredMessagePredicate();
    }

    @Test
    void nullMessageTest() {
        Assertions.assertFalse(signingWithExpiredCertificateExceptionPredicate.test(null));
    }

    @Test
    void emptyMessageTest() {
        Assertions.assertFalse(signingWithExpiredCertificateExceptionPredicate.test(""));
    }

    @Test
    void failTest() {
        Assertions.assertFalse(signingWithExpiredCertificateExceptionPredicate.test("fail-test"));
    }

    @Test
    void successTest() {
        Assertions.assertTrue(signingWithExpiredCertificateExceptionPredicate.test("The signing certificate (notBefore : 2022-12-29T08:34:56Z, notAfter : 2022-12-30T08:34:56Z) is expired at signing time 2023-05-25T07:35:18Z! Change signing certificate or use method setSignWithExpiredCertificate(true)."));
    }
}