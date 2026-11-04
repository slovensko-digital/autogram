package digital.slovensko.autogram.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.model.x509.CertificateToken;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SigningCertificateTypeTest {
    private static KeyPair keys;

    @BeforeAll
    static void createKeys() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keys = generator.generateKeyPair();
    }

    @Test
    void testTypeFollowsQualification() {
        var person = certificate(subject -> subject.addRDN(BCStyle.CN, "Jana Vzorová"));

        assertEquals(SigningCertificateType.QESIG, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESIG_QSCD, person));
        assertEquals(SigningCertificateType.QESEAL, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESEAL_QSCD, person));
        assertEquals(SigningCertificateType.ADESIG_QC, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESIG, person));
        assertEquals(SigningCertificateType.ADESEAL_QC, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESEAL, person));
        assertEquals(SigningCertificateType.ADESIG, SigningCertificateType.of(CertificateQualification.CERT_FOR_ESIG, person));
        assertEquals(SigningCertificateType.ADESEAL, SigningCertificateType.of(CertificateQualification.CERT_FOR_ESEAL, person));
    }

    @Test
    void testTypeIsUnknownWhenQualificationDoesNotTellIt() {
        var person = certificate(subject -> subject.addRDN(BCStyle.CN, "Jana Vzorová"));

        for (var qualification : List.of(CertificateQualification.QCERT_FOR_UNKNOWN_QSCD, CertificateQualification.QCERT_FOR_UNKNOWN,
                CertificateQualification.QCERT_FOR_WSA, CertificateQualification.CERT_FOR_UNKNOWN, CertificateQualification.NA))
            assertNull(SigningCertificateType.of(qualification, person), qualification.name());
        assertNull(SigningCertificateType.of(null, person));
    }

    @Test
    void testMandateCertificateAsSncaIssuesItIsMandate() {
        var mandate = certificate(subject -> subject.addRDN(BCStyle.T, "Advokát").addRDN(BCStyle.O, "MANDANT Vzorový úrad")
                .addRDN(BCStyle.ORGANIZATION_IDENTIFIER, "MANDANT NTRSK-12345678")
                .addRDN(BCStyle.CN, "Peter Novák OPRÁVNENIE 1042 Advokát"), "1.3.158.36061701.0.0.0.1.2.2", "1.3.158.36061701.1.1.1042");

        assertEquals(SigningCertificateType.QESIG_MANDATE, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESIG_QSCD, mandate));
    }

    @Test
    void testEachMandateMarkerAloneIsEnough() {
        assertTrue(SigningCertificateType.isMandate(certificate(subject -> subject.addRDN(BCStyle.CN, "Peter Novák"),
                "1.3.158.36061701.1.1.7")));
        assertTrue(SigningCertificateType.isMandate(certificate(subject -> subject.addRDN(BCStyle.CN, "Peter Novák")
                .addRDN(BCStyle.SERIALNUMBER, "MANDANT NTRSK-78910"))));
        assertTrue(SigningCertificateType.isMandate(certificate(subject -> subject.addRDN(BCStyle.CN, "Peter Novák OPRÁVNENIE 1042 Advokát"))));
        assertTrue(SigningCertificateType.isMandate(certificate(subject -> subject.addRDN(BCStyle.CN, "MANDATÁR Peter Disig Opravnenie 1 JKM"))));
    }

    @Test
    void testEmployeeCertificateIsNotMandate() {
        // SNCA's qualified certificate of an employee names the organization, but not as a mandant
        var employee = certificate(subject -> subject.addRDN(BCStyle.T, "Referent").addRDN(BCStyle.O, "Vzorový úrad")
                .addRDN(BCStyle.ORGANIZATION_IDENTIFIER, "NTRSK-12345678").addRDN(BCStyle.CN, "Jana Vzorová"),
                "1.3.158.36061701.0.0.0.1.2.2", "1.3.158.42156424.0.1.1");

        assertFalse(SigningCertificateType.isMandate(employee));
        assertEquals(SigningCertificateType.QESIG, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESIG_QSCD, employee));
    }

    @Test
    void testMandateWithoutQscdIsNotMandateType() {
        var mandate = certificate(subject -> subject.addRDN(BCStyle.CN, "Peter Novák"), "1.3.158.36061701.1.1.1042");

        assertEquals(SigningCertificateType.ADESIG_QC, SigningCertificateType.of(CertificateQualification.QCERT_FOR_ESIG, mandate));
    }

    @Test
    void testApiNamesAreCepNames() {
        var gson = new Gson();
        List<SigningCertificateType> types = gson.fromJson(
                "[\"QESig\", \"QESig-MQC\", \"QESeal\", \"AdESig-QC\", \"AdESeal-QC\", \"AdESig\", \"AdESeal\", \"QES\"]",
                new TypeToken<List<SigningCertificateType>>() { }.getType());

        assertEquals(Arrays.asList(SigningCertificateType.QESIG, SigningCertificateType.QESIG_MANDATE, SigningCertificateType.QESEAL,
                SigningCertificateType.ADESIG_QC, SigningCertificateType.ADESEAL_QC, SigningCertificateType.ADESIG,
                SigningCertificateType.ADESEAL, null), types);
    }

    private static CertificateToken certificate(Consumer<X500NameBuilder> subject, String... policies) {
        try {
            var name = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.C, "SK");
            subject.accept(name);
            var now = System.currentTimeMillis();
            var builder = new JcaX509v3CertificateBuilder(name.build(), BigInteger.ONE, new Date(now - 86_400_000L),
                    new Date(now + 86_400_000L), name.build(), keys.getPublic());
            if (policies.length > 0)
                builder.addExtension(Extension.certificatePolicies, false, new CertificatePolicies(Arrays.stream(policies)
                        .map(oid -> new PolicyInformation(new ASN1ObjectIdentifier(oid))).toArray(PolicyInformation[]::new)));
            var signer = new JcaContentSignerBuilder("SHA256withRSA").build(keys.getPrivate());
            return new CertificateToken(new JcaX509CertificateConverter().getCertificate(builder.build(signer)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
