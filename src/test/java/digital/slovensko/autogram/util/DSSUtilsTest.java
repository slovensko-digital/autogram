package digital.slovensko.autogram.util;

import digital.slovensko.autogram.TestPki;
import eu.europa.esig.dss.model.x509.CertificateToken;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DSSUtilsTest {
    @Test
    void testCertificatesWithDigitalSignatureOrNonRepudiationAreForSigning() {
        // like qualified certificates, e.g. of the Slovak eID card
        assertTrue(isSigningCertificate(new KeyUsage(KeyUsage.nonRepudiation)));
        assertTrue(isSigningCertificate(new KeyUsage(KeyUsage.digitalSignature)));
        assertTrue(isSigningCertificate(new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment)));
        assertTrue(isSigningCertificate(null));
    }

    @Test
    void testEncryptionCertificatesAreNotForSigning() {
        assertFalse(isSigningCertificate(new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.dataEncipherment)));
        assertFalse(isSigningCertificate(new KeyUsage(KeyUsage.keyAgreement)));
    }

    private static boolean isSigningCertificate(KeyUsage keyUsage) {
        var signer = TestPki.shared().issue("Key Usage " + keyUsage, TestPki.Kind.NOT_QUALIFIED, keyUsage);
        return DSSUtils.isSigningCertificate(new CertificateToken(signer.certificate()));
    }
}
