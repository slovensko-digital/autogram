package digital.slovensko.autogram;

import digital.slovensko.autogram.core.SigningKey;
import eu.europa.esig.dss.crl.CRLUtils;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.RevocationOrigin;
import eu.europa.esig.dss.enumerations.TSLType;
import eu.europa.esig.dss.model.timedependent.TimeDependentValues;
import eu.europa.esig.dss.model.tsl.*;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.revocation.crl.CRL;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLToken;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.tsl.dto.TLParsingCacheDTO;
import eu.europa.esig.dss.validation.job.cache.state.CacheStateEnum;
import eu.europa.esig.dss.validation.job.dto.AbstractCacheDTO;
import eu.europa.esig.dss.validation.job.dto.DownloadCacheDTO;
import eu.europa.esig.dss.validation.job.dto.ValidationCacheDTO;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.qualified.ETSIQCObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Test PKI that makes signatures qualified without the EU trusted lists: a CA listed as a qualified service
 * (CA/QC, granted, for e-signatures and e-seals), a qualified timestamping unit (TSA/QTST) and signers whose
 * certificates carry QcCompliance, QcSSCD and QcType. DSS then evaluates their signatures as QESig / QESeal and the
 * timestamps as QTSA. The trusted list itself is a stub (an "SK" TL that is fresh and well signed), revocation is a
 * CRL issued on the fly.
 *
 * Tests use {@link #create()} with {@link #verifier()}; the test harness keeps one on disk with {@link #loadOrCreate}
 * so its signed containers stay valid across restarts. Signer keystores are JKS with an empty password, the same as
 * the keystore Autogram opens from a file.
 */
public class TestPki {
    public static final String JANA = "Jana Vzorová";
    public static final String PETER = "Peter Ukážka";
    public static final String SEAL = "Vzorový úrad";

    static final String CA_QC = "http://uri.etsi.org/TrstSvc/Svctype/CA/QC";
    static final String QTST = "http://uri.etsi.org/TrstSvc/Svctype/TSA/QTST";
    static final String GRANTED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/granted";
    static final String FOR_ESIG = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/ForeSignatures";
    static final String FOR_ESEAL = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/ForeSeals";
    static final String CRL_URL = "http://crl.autogram.test/ca.crl"; // never fetched, crlSource() answers for it
    static final char[] PKI_PASSWORD = "harness".toCharArray();
    static final long DAY = 86_400_000L;

    public enum Kind {
        /** natural person, QcType esign */
        QESIG,
        /** legal person, QcType eseal */
        QESEAL,
        /** natural person without any QC statements */
        NOT_QUALIFIED
    }

    public record Signer(String name, X509Certificate certificate, byte[] keystore) {
        /** Token with this signer's key, like the one Autogram opens from a keystore file (e.g. for a fake TokenDriver). */
        public KeyStoreSignatureTokenConnection token() {
            return new KeyStoreSignatureTokenConnection(keystore, "JKS", new KeyStore.PasswordProtection(new char[0]));
        }

        /** Key for SigningJob. */
        public SigningKey signingKey() {
            var token = token();
            return new SigningKey(token, token.getKeys().get(0));
        }
    }

    private static TestPki shared;

    /** One PKI for the whole test run, for tests that only need some valid signer; never revoke its signers. */
    public static synchronized TestPki shared() {
        if (shared == null)
            shared = create();
        return shared;
    }

    private final String country;
    private final KeyPair caKeys;
    private final X509Certificate ca;
    private final CertificateToken caToken;
    private final KeyPair tsaKeys;
    private final X509Certificate tsa;
    private final CertificateToken tsaToken;
    private final Map<String, Signer> signers = new LinkedHashMap<>();
    private final Set<BigInteger> revoked = new HashSet<>();
    private long serial = 1000;
    private KeyPair otherTsaKeys;
    private X509Certificate otherTsa;

    private TestPki(String country, KeyPair caKeys, X509Certificate ca, KeyPair tsaKeys, X509Certificate tsa) {
        this.country = country;
        this.caKeys = caKeys;
        this.ca = ca;
        this.caToken = new CertificateToken(ca);
        this.tsaKeys = tsaKeys;
        this.tsa = tsa;
        this.tsaToken = new CertificateToken(tsa);
    }

    /** New in-memory PKI with the signers Jana Vzorová, Peter Ukážka (QESig) and Vzorový úrad (QESeal). */
    public static TestPki create() {
        return create("SK");
    }

    /** Like {@link #create()}, with the CA and its certificates from the given country, e.g. DE. */
    public static TestPki create(String country) {
        try {
            var now = System.currentTimeMillis();
            var caKeys = rsa();
            var caName = new X500Name("CN=Autogram Test Qualified CA, O=Autogram test PKI, C=" + country);
            var ext = new JcaX509ExtensionUtils();
            var ca = sign(new JcaX509v3CertificateBuilder(caName, BigInteger.ONE, new Date(now - 30 * DAY), new Date(now + 3650 * DAY), caName, caKeys.getPublic())
                    .addExtension(Extension.basicConstraints, true, new BasicConstraints(true))
                    .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign))
                    .addExtension(Extension.subjectKeyIdentifier, false, ext.createSubjectKeyIdentifier(caKeys.getPublic())), caKeys.getPrivate());

            var tsaKeys = rsa();
            var tsa = tsaCertificate("Autogram Test Qualified TSA", country, BigInteger.TWO, tsaKeys, ca, caKeys.getPrivate());

            var pki = new TestPki(country, caKeys, ca, tsaKeys, tsa);
            pki.issue(JANA, Kind.QESIG);
            pki.issue(PETER, Kind.QESIG);
            pki.issue(SEAL, Kind.QESEAL);
            return pki;
        } catch (Exception e) {
            throw new IllegalStateException("cannot create the test PKI", e);
        }
    }

    /**
     * The PKI kept in dir (ca.p12, tsa.p12 and jana.jks, peter.jks, seal.jks for the standard signers), created there
     * first if it is missing.
     */
    public static TestPki loadOrCreate(Path dir) throws Exception {
        if (!Files.exists(dir.resolve("ca.p12"))) {
            var pki = create();
            Files.createDirectories(dir);
            Files.write(dir.resolve("ca.p12"), keystore("PKCS12", "ca", pki.caKeys.getPrivate(), PKI_PASSWORD, pki.ca));
            Files.write(dir.resolve("tsa.p12"), keystore("PKCS12", "tsa", pki.tsaKeys.getPrivate(), PKI_PASSWORD, pki.tsa, pki.ca));
            for (var e : standardFiles().entrySet())
                Files.write(dir.resolve(e.getValue()), pki.signer(e.getKey()).keystore());
            return pki;
        }

        var caStore = load(Files.readAllBytes(dir.resolve("ca.p12")), "PKCS12", PKI_PASSWORD);
        var tsaStore = load(Files.readAllBytes(dir.resolve("tsa.p12")), "PKCS12", PKI_PASSWORD);
        var ca = (X509Certificate) caStore.getCertificate("ca");
        var tsa = (X509Certificate) tsaStore.getCertificate("tsa");
        var pki = new TestPki("SK", new KeyPair(ca.getPublicKey(), (PrivateKey) caStore.getKey("ca", PKI_PASSWORD)), ca,
                new KeyPair(tsa.getPublicKey(), (PrivateKey) tsaStore.getKey("tsa", PKI_PASSWORD)), tsa);
        for (var e : standardFiles().entrySet()) {
            var bytes = Files.readAllBytes(dir.resolve(e.getValue()));
            var store = load(bytes, "JKS", new char[0]);
            var cert = (X509Certificate) store.getCertificate(store.aliases().nextElement());
            pki.signers.put(e.getKey(), new Signer(e.getKey(), cert, bytes));
            pki.serial = Math.max(pki.serial, cert.getSerialNumber().longValue());
        }
        return pki;
    }

    static Map<String, String> standardFiles() {
        return Map.of(JANA, "jana.jks", PETER, "peter.jks", SEAL, "seal.jks");
    }

    public Signer signer(String name) {
        return Objects.requireNonNull(signers.get(name), "no signer " + name);
    }

    /** Issues a new certificate from the test CA (valid from a week ago for five years). */
    public Signer issue(String name, Kind kind) {
        var now = System.currentTimeMillis();
        return issue(name, kind, new Date(now - 7 * DAY), new Date(now + 1825 * DAY));
    }

    /** Issues a new certificate from the test CA valid between the given dates, e.g. an expired one. */
    public Signer issue(String name, Kind kind, Date notBefore, Date notAfter) {
        return issue(name, kind, notBefore, notAfter, new KeyUsage(KeyUsage.nonRepudiation));
    }

    /**
     * Issues a new certificate from the test CA with the given key usage (e.g. only for encryption), none when null, and
     * extended key usage, none when empty.
     */
    public Signer issue(String name, Kind kind, KeyUsage keyUsage, KeyPurposeId... extendedKeyUsage) {
        var now = System.currentTimeMillis();
        return issue(name, kind, new Date(now - 7 * DAY), new Date(now + 1825 * DAY), keyUsage, extendedKeyUsage);
    }

    private Signer issue(String name, Kind kind, Date notBefore, Date notAfter, KeyUsage keyUsage, KeyPurposeId... extendedKeyUsage) {
        try {
            var keys = rsa();
            var id = ++serial;
            var subject = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.C, country);
            if (kind == Kind.QESEAL) {
                subject.addRDN(BCStyle.O, name).addRDN(BCStyle.ORGANIZATION_IDENTIFIER, "NTR" + country + "-" + String.format("%08d", id));
            } else {
                var parts = name.split(" ", 2);
                subject.addRDN(BCStyle.GIVENNAME, parts[0]).addRDN(BCStyle.SURNAME, parts.length > 1 ? parts[1] : parts[0])
                        .addRDN(BCStyle.SERIALNUMBER, "PNO" + country + "-TEST" + String.format("%06d", id));
            }
            subject.addRDN(BCStyle.CN, name);

            var ext = new JcaX509ExtensionUtils();
            var crl = new DistributionPoint(new DistributionPointName(new GeneralNames(
                    new GeneralName(GeneralName.uniformResourceIdentifier, CRL_URL))), null, null);
            var builder = new JcaX509v3CertificateBuilder(ca, BigInteger.valueOf(id), notBefore, notAfter, subject.build(), keys.getPublic())
                    .addExtension(Extension.basicConstraints, true, new BasicConstraints(false))
                    .addExtension(Extension.subjectKeyIdentifier, false, ext.createSubjectKeyIdentifier(keys.getPublic()))
                    .addExtension(Extension.authorityKeyIdentifier, false, ext.createAuthorityKeyIdentifier(ca))
                    .addExtension(Extension.cRLDistributionPoints, false, new CRLDistPoint(new DistributionPoint[]{crl}));
            if (keyUsage != null)
                builder.addExtension(Extension.keyUsage, true, keyUsage);
            if (extendedKeyUsage.length > 0)
                builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(extendedKeyUsage));
            if (kind != Kind.NOT_QUALIFIED) {
                var qc = new ASN1EncodableVector();
                qc.add(new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcCompliance));
                qc.add(new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcSSCD));
                qc.add(new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcType, new DERSequence(
                        kind == Kind.QESEAL ? ETSIQCObjectIdentifiers.id_etsi_qct_eseal : ETSIQCObjectIdentifiers.id_etsi_qct_esign)));
                // QCP-l-qscd for seals, QCP-n-qscd for signatures
                builder.addExtension(Extension.certificatePolicies, false, new CertificatePolicies(new PolicyInformation(
                                new ASN1ObjectIdentifier(kind == Kind.QESEAL ? "0.4.0.194112.1.3" : "0.4.0.194112.1.2"))))
                        .addExtension(Extension.qCStatements, false, new DERSequence(qc));
            }
            var cert = sign(builder, caKeys.getPrivate());
            var alias = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                    .toLowerCase().replace(' ', '-');
            var signer = new Signer(name, cert, keystore("JKS", alias, keys.getPrivate(), new char[0], cert, ca));
            signers.put(name, signer);
            return signer;
        } catch (Exception e) {
            throw new IllegalStateException("cannot issue a certificate for " + name, e);
        }
    }

    /** The next CRL lists this signer's certificate as revoked. */
    public void revoke(Signer signer) {
        revoked.add(signer.certificate().getSerialNumber());
    }

    public X509Certificate caCertificate() {
        return ca;
    }

    /** Offline verifier that trusts only this PKI, for SignatureValidator.useCustomVerifier. */
    public CertificateVerifier verifier() {
        var verifier = new CommonCertificateVerifier();
        verifier.setTrustedCertSources(trustedListsSource(null, null));
        verifier.setCrlSource(crlSource(null));
        verifier.setOcspSource(null);
        verifier.setAIASource(null);
        return verifier;
    }

    /**
     * Trusted list entries for the CA and the TSA. With tl == null they hang on a stub SK trusted list; the harness
     * passes a really loaded one when it adds this PKI next to the EU trusted lists.
     */
    public TrustedListsCertificateSource trustedListsSource(LOTLInfo lotl, TLInfo tl) {
        TLValidationJobSummary summary = null;
        if (tl == null) {
            lotl = new LOTLInfo(download(), parsing("EU", "http://uri.etsi.org/TrstSvc/TrustedList/TSLType/EUlistofthelists"),
                    validation(), "http://tl.autogram.test/lotl.xml");
            tl = new TLInfo(download(), parsing("SK", "http://uri.etsi.org/TrstSvc/TrustedList/TSLType/EUgeneric"),
                    validation(), "http://tl.autogram.test/tl-sk.xml", lotl);
            lotl.setTlInfos(List.of(tl));
            summary = new TLValidationJobSummary(List.of(lotl), List.of());
        }

        var tsp = new TrustServiceProvider();
        tsp.setNames(Map.of("EN", List.of("Autogram test PKI")));
        var source = new TrustedListsCertificateSource();
        if (summary != null)
            source.setSummary(summary);
        source.setTrustPropertiesByCertificates(Map.of(
                caToken, List.of(new TrustProperties(lotl, tl, tsp, service("Autogram Test Qualified CA", CA_QC, List.of(FOR_ESIG, FOR_ESEAL), ca))),
                tsaToken, List.of(new TrustProperties(lotl, tl, tsp, service("Autogram Test Qualified TSA", QTST, List.of(), tsa)))));
        source.setTrustTimeByCertificates(Map.of(
                caToken, List.of(new CertificateTrustTime(ca.getNotBefore(), null)),
                tsaToken, List.of(new CertificateTrustTime(tsa.getNotBefore(), null))));
        return source;
    }

    /** CRLs for certificates of this CA, issued on the fly; others go to delegate (none when it is null). */
    public CRLSource crlSource(RevocationSource<CRL> delegate) {
        return (cert, issuer) -> {
            if (issuer == null || !issuer.equals(caToken))
                return delegate == null ? null : (CRLToken) delegate.getRevocationToken(cert, issuer);
            try {
                var token = new CRLToken(cert, CRLUtils.buildCRLValidity(CRLUtils.buildCRLBinary(crl()), issuer));
                token.setExternalOrigin(RevocationOrigin.EXTERNAL);
                return token;
            } catch (IOException e) {
                throw new IllegalStateException("cannot build the test CRL", e);
            }
        };
    }

    /** In-process qualified timestamps, e.g. for SigningJob.signWithKey with BASELINE_T. */
    public TSPSource tspSource() {
        return tspSource(tsaKeys, tsa);
    }

    /** Timestamps from a TSA of the same CA that is not listed as a qualified service (TSA/QTST). */
    public synchronized TSPSource nonQualifiedTspSource() {
        if (otherTsa == null) {
            try {
                otherTsaKeys = rsa();
                otherTsa = tsaCertificate("Autogram Test TSA", country, BigInteger.valueOf(3), otherTsaKeys, ca, caKeys.getPrivate());
            } catch (Exception e) {
                throw new IllegalStateException("cannot create the non-qualified test TSA", e);
            }
        }
        return tspSource(otherTsaKeys, otherTsa);
    }

    /** RFC 3161 reply from the qualified test TSA (the harness serves it over HTTP for Autogram's TSA setting). */
    public byte[] timestampReply(byte[] encodedRequest) throws Exception {
        return timestampReply(encodedRequest, tsaKeys, tsa);
    }

    private TSPSource tspSource(KeyPair keys, X509Certificate certificate) {
        return (digestAlgorithm, digest) -> {
            try {
                var request = new org.bouncycastle.tsp.TimeStampRequestGenerator();
                request.setCertReq(true);
                var reply = new org.bouncycastle.tsp.TimeStampResponse(timestampReply(
                        request.generate(new ASN1ObjectIdentifier(digestAlgorithm.getOid()), digest).getEncoded(), keys, certificate));
                return new eu.europa.esig.dss.model.TimestampBinary(reply.getTimeStampToken().getEncoded());
            } catch (Exception e) {
                throw new IllegalStateException("test timestamp failed", e);
            }
        };
    }

    private byte[] timestampReply(byte[] encodedRequest, KeyPair keys, X509Certificate certificate) throws Exception {
        var request = new TimeStampRequest(encodedRequest);
        var digest = new JcaDigestCalculatorProviderBuilder().build().get(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256));
        var signer = new JcaSimpleSignerInfoGeneratorBuilder().build("SHA256withRSA", keys.getPrivate(), certificate);
        var generator = new TimeStampTokenGenerator(signer, digest, new ASN1ObjectIdentifier("1.3.6.1.4.1.99999.1"));
        generator.addCertificates(new JcaCertStore(List.of(certificate, ca)));
        return new TimeStampResponseGenerator(generator, TSPAlgorithms.ALLOWED)
                .generate(request, BigInteger.valueOf(System.nanoTime()), new Date()).getEncoded();
    }

    private static X509Certificate tsaCertificate(String name, String country, BigInteger serialNumber, KeyPair keys,
            X509Certificate ca, PrivateKey caKey) throws Exception {
        var now = System.currentTimeMillis();
        var ext = new JcaX509ExtensionUtils();
        return sign(new JcaX509v3CertificateBuilder(ca, serialNumber, new Date(now - 30 * DAY), new Date(now + 3650 * DAY),
                new X500Name("CN=" + name + ", O=Autogram test PKI, C=" + country), keys.getPublic())
                .addExtension(Extension.basicConstraints, true, new BasicConstraints(false))
                .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation))
                .addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping))
                .addExtension(Extension.subjectKeyIdentifier, false, ext.createSubjectKeyIdentifier(keys.getPublic()))
                .addExtension(Extension.authorityKeyIdentifier, false, ext.createAuthorityKeyIdentifier(ca)), caKey);
    }

    private byte[] crl() {
        try {
            var now = System.currentTimeMillis();
            var builder = new X509v2CRLBuilder(new JcaX509CertificateHolder(ca).getSubject(), new Date(now - 60_000));
            builder.setNextUpdate(new Date(now + DAY));
            builder.addExtension(Extension.cRLNumber, false, new CRLNumber(BigInteger.valueOf(now)));
            builder.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(ca));
            for (var serialNumber : revoked)
                builder.addCRLEntry(serialNumber, new Date(now - 60_000), CRLReason.keyCompromise);
            return builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(caKeys.getPrivate())).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("cannot build the test CRL", e);
        }
    }

    private static TimeDependentValues<TrustServiceStatusAndInformationExtensions> service(String name, String type,
            List<String> additionalInfo, X509Certificate cert) {
        return new TimeDependentValues<>(List.of(new TrustServiceStatusAndInformationExtensions.TrustServiceStatusAndInformationExtensionsBuilder()
                .setNames(Map.of("EN", List.of(name)))
                .setType(type)
                .setStatus(GRANTED)
                .setAdditionalServiceInfoUris(additionalInfo)
                .setConditionsForQualifiers(List.of())
                .setServiceSupplyPoints(List.of())
                .setStartDate(cert.getNotBefore())
                .build()));
    }

    private static <T extends AbstractCacheDTO> T synced(T record) {
        var now = new Date();
        record.setCacheState(CacheStateEnum.SYNCHRONIZED);
        record.setResultExist(true);
        record.setLastStateTransitionTime(now);
        record.setLastSuccessSynchronizationTime(now);
        return record;
    }

    private static DownloadCacheDTO download() {
        return synced(new DownloadCacheDTO());
    }

    private static TLParsingCacheDTO parsing(String territory, String type) {
        var now = System.currentTimeMillis();
        var parsing = synced(new TLParsingCacheDTO());
        parsing.setTerritory(territory);
        parsing.setTSLType(TSLType.fromUri(type));
        parsing.setVersion(6);
        parsing.setSequenceNumber(1);
        parsing.setIssueDate(new Date(now - DAY));
        parsing.setNextUpdateDate(new Date(now + 180 * DAY));
        parsing.setDistributionPoints(List.of());
        parsing.setTrustServiceProviders(List.of());
        parsing.setLotlOtherPointers(List.of());
        parsing.setTlOtherPointers(List.of());
        parsing.setPivotUrls(List.of());
        return parsing;
    }

    private static ValidationCacheDTO validation() {
        var validation = synced(new ValidationCacheDTO());
        validation.setIndication(Indication.TOTAL_PASSED);
        validation.setSigningTime(new Date());
        validation.setPotentialSigners(List.of());
        return validation;
    }

    private static KeyPair rsa() throws NoSuchAlgorithmException {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static X509Certificate sign(X509v3CertificateBuilder builder, PrivateKey key) throws Exception {
        return new JcaX509CertificateConverter().getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(key)));
    }

    private static byte[] keystore(String type, String alias, PrivateKey key, char[] password, Certificate... chain) throws Exception {
        var store = KeyStore.getInstance(type);
        store.load(null, null);
        store.setKeyEntry(alias, key, password, chain);
        var out = new ByteArrayOutputStream();
        store.store(out, password);
        return out.toByteArray();
    }

    private static KeyStore load(byte[] bytes, String type, char[] password) throws Exception {
        var store = KeyStore.getInstance(type);
        store.load(new ByteArrayInputStream(bytes), password);
        return store;
    }
}
