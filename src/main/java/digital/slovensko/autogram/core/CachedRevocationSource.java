package digital.slovensko.autogram.core;

import eu.europa.esig.dss.crl.CRLUtils;
import eu.europa.esig.dss.enumerations.RevocationOrigin;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.revocation.Revocation;
import eu.europa.esig.dss.model.x509.revocation.crl.CRL;
import eu.europa.esig.dss.model.x509.revocation.ocsp.OCSP;
import eu.europa.esig.dss.spi.DSSRevocationUtils;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSource;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSourceAlternateUrlsSupport;
import eu.europa.esig.dss.spi.x509.revocation.RevocationToken;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLToken;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caches signed evidence, not validation decisions. Each caller gets its own DSS token. */
abstract class CachedRevocationSource<R extends Revocation> implements RevocationSourceAlternateUrlsSupport<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedRevocationSource.class);
    private static final Duration MAX_AGE = Duration.ofMinutes(5);
    private static final int MAX_ENTRIES = 256;
    private static final int MAX_BYTES = 16 * 1024 * 1024;

    private final RevocationSource<R> online;
    private final Clock clock;
    private final Map<Key, Entry> entries = new LinkedHashMap<>(16, .75f, true);

    private record Key(String certificate, String issuer, List<String> alternativeUrls) { }
    protected record Entry(byte[] encoded, String url, CertificateToken signer, Instant fetched, Instant expires) { }

    CachedRevocationSource(RevocationSource<R> online, Clock clock) {
        this.online = online;
        this.clock = clock;
    }

    static CachedRevocationSource<CRL> crl(RevocationSource<CRL> online, Clock clock) {
        return new CachedRevocationSource<>(online, clock) {
            @Override
            protected CRLToken decode(Entry entry, CertificateToken certificate, CertificateToken issuer) throws Exception {
                var signer = entry.signer() != null ? entry.signer() : issuer;
                var validity = CRLUtils.buildCRLValidity(CRLUtils.buildCRLBinary(entry.encoded()), signer);
                validity.setIssuerToken(signer);
                return new CRLToken(certificate, validity);
            }
        };
    }

    static CachedRevocationSource<OCSP> ocsp(RevocationSource<OCSP> online, Clock clock) {
        return new CachedRevocationSource<>(online, clock) {
            @Override
            protected OCSPToken decode(Entry entry, CertificateToken certificate, CertificateToken issuer) throws Exception {
                var response = (BasicOCSPResp) new OCSPResp(entry.encoded()).getResponseObject();
                var single = DSSRevocationUtils.getLatestSingleResponse(response, certificate, issuer);
                return new OCSPToken(response, single, certificate, issuer);
            }
        };
    }

    protected abstract RevocationToken<R> decode(Entry entry, CertificateToken certificate, CertificateToken issuer) throws Exception;

    @Override
    public RevocationToken<R> getRevocationToken(CertificateToken certificate, CertificateToken issuer) {
        return getRevocationToken(certificate, issuer, List.of());
    }

    @Override
    public synchronized RevocationToken<R> getRevocationToken(CertificateToken certificate, CertificateToken issuer,
            List<String> alternativeUrls) {
        if (certificate == null || issuer == null)
            return fetch(certificate, issuer, alternativeUrls);

        var key = new Key(certificate.getDSSIdAsString(), issuer.getDSSIdAsString(), List.copyOf(alternativeUrls));
        var now = clock.instant();
        var entry = entries.get(key);
        if (entry != null) {
            if (!now.isBefore(entry.fetched()) && now.isBefore(entry.expires())) {
                try {
                    var token = decode(entry, certificate, issuer);
                    if (token.isValid()) {
                        token.setExternalOrigin(RevocationOrigin.CACHED);
                        token.setSourceURL(entry.url());
                        return token;
                    }
                } catch (Exception e) {
                    LOGGER.debug("Cannot reconstruct cached revocation evidence", e);
                }
            }
            entries.remove(key);
        }

        var token = fetch(certificate, issuer, alternativeUrls);
        now = clock.instant();
        // No invented validity period for responses without nextUpdate. DSS still decides whether
        // freshly downloaded evidence is acceptable for this particular signature and validation time.
        if (token != null && token.isValid() && token.getStatus() != null && token.getStatus().isKnown()
                && token.getThisUpdate() != null && !token.getThisUpdate().toInstant().isAfter(now)
                && token.getNextUpdate() != null && token.getNextUpdate().toInstant().isAfter(now)
                && (token.getProductionDate() == null || !token.getProductionDate().toInstant().isAfter(now))) {
            var encoded = token.getEncoded();
            if (encoded.length <= MAX_BYTES) {
                var expires = token.getNextUpdate().toInstant();
                if (expires.isAfter(now.plus(MAX_AGE)))
                    expires = now.plus(MAX_AGE);
                entries.put(key, new Entry(encoded.clone(), token.getSourceURL(), token.getIssuerCertificateToken(), now, expires));
                entries.values().removeIf(value -> !clock.instant().isBefore(value.expires()));
                long bytes = entries.values().stream().mapToLong(value -> value.encoded().length).sum();
                var oldest = entries.values().iterator();
                while (entries.size() > MAX_ENTRIES || bytes > MAX_BYTES) {
                    bytes -= oldest.next().encoded().length;
                    oldest.remove();
                }
            }
        }
        return token;
    }

    private RevocationToken<R> fetch(CertificateToken certificate, CertificateToken issuer, List<String> alternativeUrls) {
        if (online instanceof RevocationSourceAlternateUrlsSupport<R> withAlternatives)
            return withAlternatives.getRevocationToken(certificate, issuer, alternativeUrls);
        return online.getRevocationToken(certificate, issuer);
    }
}
