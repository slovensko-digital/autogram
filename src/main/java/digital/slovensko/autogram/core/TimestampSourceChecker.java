package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.TsaServerMisconfiguredException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.model.DigestDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.timestamp.DetachedTimestampValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Checks an actual TSA response, not its URL. The probe contains a random digest, never the user's document. */
public class TimestampSourceChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimestampSourceChecker.class);
    private final Supplier<CompletableFuture<CertificateVerifier>> trustedLists;

    /** A null qualification means that the source could not be checked. */
    public record Result(TimestampQualification qualification, TSPSource source) { }

    public TimestampSourceChecker(Supplier<CompletableFuture<CertificateVerifier>> trustedLists) {
        this.trustedLists = trustedLists;
    }

    public CompletableFuture<Result> checkAsync(TSPSource source, Duration sourceTimeout, Duration trustedListsTimeout) {
        var unknown = new Result(null, source);
        return trustedLists.get().thenCompose(verifier -> CompletableFuture.supplyAsync(() -> check(source, verifier))
                        .completeOnTimeout(unknown, sourceTimeout.toMillis(), TimeUnit.MILLISECONDS))
                .completeOnTimeout(unknown, trustedListsTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(error -> unknown);
    }

    public Result check(TSPSource source, CertificateVerifier verifier) {
        if (source == null || verifier == null || verifier.getTrustedCertSources().getNumberOfCertificates() == 0)
            return new Result(null, source);

        try {
            var digest = new byte[32];
            new SecureRandom().nextBytes(digest);
            var algorithm = DigestAlgorithm.SHA256;
            var qualification = qualificationOf(source.getTimeStampResponse(algorithm, digest), algorithm, digest, verifier);
            // A fallback server could return a different kind of timestamp when the document is really signed.
            // Do not silently downgrade a choice that the picker advertised as qualified.
            TSPSource checkedSource = qualification == TimestampQualification.QTSA ? (digestAlgorithm, digestValue) -> {
                var timestamp = source.getTimeStampResponse(digestAlgorithm, digestValue);
                if (qualificationOf(timestamp, digestAlgorithm, digestValue, verifier) != TimestampQualification.QTSA)
                    throw new TsaServerMisconfiguredException(TsaServerMisconfiguredException.Error.NOT_QUALIFIED, null);
                return timestamp;
            } : source;
            return new Result(qualification, checkedSource);
        } catch (RuntimeException e) {
            LOGGER.debug("Timestamp source qualification check failed", e);
            return new Result(null, source);
        }
    }

    private static TimestampQualification qualificationOf(TimestampBinary timestamp, DigestAlgorithm algorithm,
            byte[] digest, CertificateVerifier verifier) {
        if (timestamp == null)
            return null;

        var validator = new DetachedTimestampValidator(new InMemoryDocument(timestamp.getBytes()));
        validator.setTimestampedData(new DigestDocument(algorithm, digest));
        validator.setCertificateVerifier(verifier);
        var report = validator.validateDocument().getSimpleReport();
        var id = report.getFirstTimestampId();
        return id != null && report.getIndication(id) == Indication.PASSED ? report.getTimestampQualification(id) : null;
    }
}
