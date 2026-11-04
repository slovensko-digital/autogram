package digital.slovensko.autogram.core;

import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.AIASource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.validation.CertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.security.auth.x500.X500Principal;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Current revocation status of a signing certificate, from OCSP or else from CRL, and its qualification by the
 * trusted lists Autogram validates signatures with.
 */
public class CertificateStatusChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateStatusChecker.class);

    public enum Qualification { QUALIFIED, NOT_QUALIFIED, UNKNOWN }

    /** @param type null when the qualification does not tell it */
    public record Result(CertificateStatus status, Qualification qualification, SigningCertificateType type) {
        public Result(CertificateStatus status, Qualification qualification) {
            this(status, qualification, null);
        }
    }

    private final OCSPSource ocspSource;
    private final CRLSource crlSource;
    private final AIASource aiaSource;
    private final Supplier<CompletableFuture<CertificateVerifier>> trustedLists;

    /** @param trustedLists the verifier with the trusted lists once they are loaded */
    public CertificateStatusChecker(OCSPSource ocspSource, CRLSource crlSource, AIASource aiaSource,
            Supplier<CompletableFuture<CertificateVerifier>> trustedLists) {
        this.ocspSource = ocspSource;
        this.crlSource = crlSource;
        this.aiaSource = aiaSource;
        this.trustedLists = trustedLists;
    }

    public static CertificateStatusChecker online() {
        return new CertificateStatusChecker(new OnlineOCSPSource(NetworkClients.ocspDataLoader()),
                new OnlineCRLSource(NetworkClients.dataLoader()), new DefaultAIASource(NetworkClients.dataLoader()),
                SignatureValidator.getInstance()::whenLoaded);
    }

    /**
     * Both checks run in parallel, each one that does not finish in time is UNKNOWN. The qualification waits for the
     * trusted lists, which can take a while right after Autogram starts.
     */
    public CompletableFuture<Result> checkAsync(DSSPrivateKeyEntry key, Duration statusTimeout, Duration trustedListsTimeout) {
        var status = withTimeout(CompletableFuture.supplyAsync(() -> checkStatus(key)), CertificateStatus.UNKNOWN, statusTimeout);
        var qualification = withTimeout(trustedLists.get().thenApplyAsync(verifier -> certificateQualification(key, verifier)),
                null, trustedListsTimeout);
        return status.thenCombine(qualification, (s, q) ->
                new Result(s, toQualification(q), SigningCertificateType.of(q, key.getCertificate())));
    }

    private static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future, T fallback, Duration timeout) {
        return future.completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS).exceptionally(e -> fallback);
    }

    /** UNKNOWN when neither OCSP nor CRL gives a valid answer, e.g. offline. */
    public CertificateStatus checkStatus(DSSPrivateKeyEntry key) {
        var certificate = key.getCertificate();
        var issuer = findIssuer(certificate, key.getCertificateChain());
        if (issuer.isEmpty())
            return CertificateStatus.UNKNOWN;

        for (var source : List.<RevocationSource<?>>of(ocspSource, crlSource)) {
            var status = statusFrom(source, certificate, issuer.get());
            if (status.isKnown())
                return status;
        }

        return CertificateStatus.UNKNOWN;
    }

    /** UNKNOWN when the trusted lists could not be loaded. */
    public Qualification checkQualification(DSSPrivateKeyEntry key, CertificateVerifier verifier) {
        return toQualification(certificateQualification(key, verifier));
    }

    /** Null when the type is unknown, see {@link SigningCertificateType#of}. */
    public SigningCertificateType checkType(DSSPrivateKeyEntry key, CertificateVerifier verifier) {
        return SigningCertificateType.of(certificateQualification(key, verifier), key.getCertificate());
    }

    private static Qualification toQualification(CertificateQualification qualification) {
        if (qualification == null)
            return Qualification.UNKNOWN;

        return qualification.isQc() && qualification != CertificateQualification.QCERT_FOR_WSA
                ? Qualification.QUALIFIED : Qualification.NOT_QUALIFIED;
    }

    /** Null when the trusted lists could not be loaded. */
    private CertificateQualification certificateQualification(DSSPrivateKeyEntry key, CertificateVerifier verifier) {
        if (verifier == null || verifier.getTrustedCertSources().getNumberOfCertificates() == 0)
            return null;

        // revocation is checked on its own, the trusted lists and the chain are enough for the qualification
        var chainOnlyVerifier = new CommonCertificateVerifier();
        chainOnlyVerifier.setTrustedCertSources(verifier.getTrustedCertSources());
        chainOnlyVerifier.setAdjunctCertSources(chainSource(key));
        chainOnlyVerifier.setAIASource(aiaSource);
        chainOnlyVerifier.setOcspSource(null);
        chainOnlyVerifier.setCrlSource(null);

        try {
            var validator = CertificateValidator.fromCertificate(key.getCertificate());
            validator.setCertificateVerifier(chainOnlyVerifier);
            var qualification = validator.validate().getSimpleReport().getQualificationAtValidationTime();
            return qualification != null ? qualification : CertificateQualification.NA;
        } catch (RuntimeException e) {
            LOGGER.debug("Qualification check of {} failed", key.getCertificate().getDSSIdAsString(), e);
            return null;
        }
    }

    /** ISO code of the issuer's country, the country whose trusted list makes the certificate qualified. */
    public static Optional<String> issuerCountry(CertificateToken certificate) {
        try {
            return new LdapName(certificate.getIssuerX500Principal().getName(X500Principal.RFC2253)).getRdns().stream()
                    .filter(rdn -> rdn.getType().equalsIgnoreCase("C"))
                    .map(rdn -> rdn.getValue().toString().toUpperCase(Locale.ROOT))
                    .findFirst();
        } catch (InvalidNameException e) {
            return Optional.empty();
        }
    }

    private static CommonCertificateSource chainSource(DSSPrivateKeyEntry key) {
        var source = new CommonCertificateSource();
        if (key.getCertificateChain() != null)
            for (var certificate : key.getCertificateChain())
                source.addCertificate(certificate);

        return source;
    }

    private static CertificateStatus statusFrom(RevocationSource<?> source, CertificateToken certificate, CertificateToken issuer) {
        try {
            var token = source.getRevocationToken(certificate, issuer);
            if (token != null && token.isValid() && token.getStatus() != null)
                return token.getStatus();
        } catch (RuntimeException e) {
            LOGGER.debug("Revocation check of {} failed", certificate.getDSSIdAsString(), e);
        }

        return CertificateStatus.UNKNOWN;
    }

    private Optional<CertificateToken> findIssuer(CertificateToken certificate, CertificateToken[] chain) {
        if (certificate.isSelfSigned())
            return Optional.empty();

        var issuer = signerOf(certificate, chain == null ? List.of() : List.of(chain));
        if (issuer.isPresent() || aiaSource == null)
            return issuer;

        try {
            return signerOf(certificate, aiaSource.getCertificatesByAIA(certificate));
        } catch (RuntimeException e) {
            LOGGER.debug("Issuer of {} could not be downloaded", certificate.getDSSIdAsString(), e);
            return Optional.empty();
        }
    }

    private static Optional<CertificateToken> signerOf(CertificateToken certificate, Collection<CertificateToken> candidates) {
        return candidates.stream()
                .filter(candidate -> !candidate.equals(certificate) && certificate.isSignedBy(candidate))
                .findFirst();
    }
}
