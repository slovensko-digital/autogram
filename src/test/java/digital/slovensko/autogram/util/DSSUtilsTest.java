package digital.slovensko.autogram.util;

import digital.slovensko.autogram.TestPki;
import eu.europa.esig.dss.model.x509.CertificateToken;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DSSUtilsTest {
    @Test
    void testCertificatesWithDigitalSignatureOrNonRepudiationAreForSigning() {
        // like the qualified certificate of the eID card read over NFC
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

    @Test
    void testCertificatesOnlyForTlsServersTimestampsOrOcspAreNotForSigning() {
        var signing = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation);

        // like Autogram's own certificate for HTTPS on localhost
        assertFalse(isSigningCertificate(new KeyUsage(KeyUsage.nonRepudiation | KeyUsage.digitalSignature | KeyUsage.keyEncipherment),
                KeyPurposeId.id_kp_serverAuth));
        assertFalse(isSigningCertificate(signing, KeyPurposeId.id_kp_timeStamping));
        assertFalse(isSigningCertificate(signing, KeyPurposeId.id_kp_OCSPSigning));
        assertFalse(isSigningCertificate(signing, KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_timeStamping));
    }

    @Test
    void testCertificatesWithAnotherExtendedKeyUsageAreForSigning() {
        var signing = new KeyUsage(KeyUsage.nonRepudiation);

        assertTrue(isSigningCertificate(signing, KeyPurposeId.id_kp_emailProtection, KeyPurposeId.id_kp_clientAuth));
        assertTrue(isSigningCertificate(signing, KeyPurposeId.id_kp_emailProtection));
        assertTrue(isSigningCertificate(signing, KeyPurposeId.id_kp_clientAuth));
        assertTrue(isSigningCertificate(signing, KeyPurposeId.id_kp_codeSigning));
        assertTrue(isSigningCertificate(signing, KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth));
        assertTrue(isSigningCertificate(signing, KeyPurposeId.anyExtendedKeyUsage));
        // RFC 9336 document signing
        assertTrue(isSigningCertificate(signing, KeyPurposeId.getInstance(new ASN1ObjectIdentifier("1.3.6.1.5.5.7.3.36"))));
    }

    private static boolean isSigningCertificate(KeyUsage keyUsage, KeyPurposeId... extendedKeyUsage) {
        var signer = TestPki.shared().issue("Key Usage " + keyUsage, TestPki.Kind.NOT_QUALIFIED, keyUsage, extendedKeyUsage);
        return DSSUtils.isSigningCertificate(new CertificateToken(signer.certificate()));
    }
}
