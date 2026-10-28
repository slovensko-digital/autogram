package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.settings.Country;
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
import java.util.Set;
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

    public record Result(CertificateStatus status, Qualification qualification) { }

    private final OCSPSource ocspSource;
    private final CRLSource crlSource;
    private final AIASource aiaSource;
    private final Supplier<CompletableFuture<CertificateVerifier>> trustedLists;
    private final Supplier<Set<String>> countriesNotLoaded;

    /** @param trustedLists the verifier with the trusted lists once they are loaded */
    public CertificateStatusChecker(OCSPSource ocspSource, CRLSource crlSource, AIASource aiaSource,
            Supplier<CompletableFuture<CertificateVerifier>> trustedLists) {
        this(ocspSource, crlSource, aiaSource, trustedLists, Set::of);
    }

    /** @param countriesNotLoaded countries whose trusted lists were not loaded, as in {@link SignatureValidator.FailedTLs} */
    public CertificateStatusChecker(OCSPSource ocspSource, CRLSource crlSource, AIASource aiaSource,
            Supplier<CompletableFuture<CertificateVerifier>> trustedLists, Supplier<Set<String>> countriesNotLoaded) {
        this.ocspSource = ocspSource;
        this.crlSource = crlSource;
        this.aiaSource = aiaSource;
        this.trustedLists = trustedLists;
        this.countriesNotLoaded = countriesNotLoaded;
    }

    public static CertificateStatusChecker online() {
        var validator = SignatureValidator.getInstance();
        return new CertificateStatusChecker(new OnlineOCSPSource(NetworkClients.ocspDataLoader()),
                new OnlineCRLSource(NetworkClients.dataLoader()), new DefaultAIASource(NetworkClients.dataLoader()),
                validator::whenLoaded, () -> validator.getFailedTLs().countries());
    }

    /**
     * Both checks run in parallel, each one that does not finish in time is UNKNOWN. The qualification waits for the
     * trusted lists, which can take a while right after Autogram starts.
     */
    public CompletableFuture<Result> checkAsync(DSSPrivateKeyEntry key, Duration statusTimeout, Duration trustedListsTimeout) {
        var status = withTimeout(CompletableFuture.supplyAsync(() -> checkStatus(key)), CertificateStatus.UNKNOWN, statusTimeout);
        var qualification = withTimeout(trustedLists.get().thenApplyAsync(verifier -> checkQualification(key, verifier)),
                Qualification.UNKNOWN, trustedListsTimeout);
        return status.thenCombine(qualification, Result::new);
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

    /** UNKNOWN when the trusted lists, or just the one of the issuer's country, could not be loaded. */
    public Qualification checkQualification(DSSPrivateKeyEntry key, CertificateVerifier verifier) {
        if (verifier == null || verifier.getTrustedCertSources().getNumberOfCertificates() == 0)
            return Qualification.UNKNOWN;

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
            if (qualification != null && qualification.isQc() && qualification != CertificateQualification.QCERT_FOR_WSA)
                return Qualification.QUALIFIED;

            // the list that could make it qualified is missing, e.g. its server did not answer in time
            return isIssuerCountryNotLoaded(key.getCertificate()) ? Qualification.UNKNOWN : Qualification.NOT_QUALIFIED;
        } catch (RuntimeException e) {
            LOGGER.debug("Qualification check of {} failed", key.getCertificate().getDSSIdAsString(), e);
            return Qualification.UNKNOWN;
        }
    }

    private boolean isIssuerCountryNotLoaded(CertificateToken certificate) {
        var notLoaded = countriesNotLoaded.get();
        return issuerCountry(certificate)
                .flatMap(Country::withTrustedList)
                .map(country -> notLoaded.contains(country.getShortname()))
                .orElse(false);
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
