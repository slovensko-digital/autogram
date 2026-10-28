package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.http.ResponseEnvelope;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.CommonsHttpClientResponseHandler;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.policy.SignaturePolicyProvider;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Autogram-only factories; never change JVM-wide networking settings. */
public final class NetworkClients {
    private static ProxyConfiguration startup;

    private NetworkClients() { }

    public static synchronized void initialize(ProxyConfiguration configuration) {
        if (startup != null)
            throw new IllegalStateException("Networking is already initialized; restart to change proxy.");
        startup = Objects.requireNonNull(configuration);
    }

    public static synchronized ProxyConfiguration configuration() {
        return startup == null ? ProxyConfiguration.NONE : startup;
    }

    public static DataLoader dataLoader() {
        return configure(trustingSystem(new CommonsDataLoader()), configuration());
    }

    public static DataLoader timestampDataLoader() {
        return configure(trustingSystem(new TimestampDataLoader()), configuration());
    }

    public static DataLoader ocspDataLoader() {
        return configure(trustingSystem(new OCSPDataLoader()), configuration());
    }

    private static CommonsDataLoader trustingSystem(CommonsDataLoader loader) {
        loader.setTrustStrategy(SystemTrust.trustStrategy());
        return loader;
    }

    public static FileCacheDataLoader fileCacheDataLoader(long expirationTime) {
        return fileCacheDataLoader(expirationTime, dataLoader());
    }

    /** Each download fails that does not start within the grace period or is slower than the minimum after it. */
    public static FileCacheDataLoader fileCacheDataLoader(long expirationTime, Duration grace, long minBytesPerSecond) {
        return fileCacheDataLoader(expirationTime,
                configure(trustingSystem(new SlowDownloadAbortingDataLoader(grace, minBytesPerSecond)), configuration()));
    }

    private static FileCacheDataLoader fileCacheDataLoader(long expirationTime, DataLoader loader) {
        var cache = new FileCacheDataLoader();
        cache.setCacheExpirationTime(expirationTime);
        cache.setDataLoader(loader);
        return cache;
    }

    static DataLoader configure(CommonsDataLoader loader, ProxyConfiguration configuration) {
        if (!configuration.isConfigured())
            return loader;

        var properties = new ProxyProperties();
        properties.setScheme("http");
        properties.setHost(configuration.host());
        properties.setPort(configuration.port());
        var proxy = new ProxyConfig();
        proxy.setHttpProperties(properties);
        proxy.setHttpsProperties(properties);
        loader.setProxyConfig(proxy);
        return new HttpOnlyDataLoader(loader);
    }

    public static CommonCertificateVerifier certificateVerifier() {
        var verifier = new CommonCertificateVerifier();
        // DSS's default AIA loader is NativeHTTPDataLoader, not CommonsDataLoader.
        verifier.setAIASource(new DefaultAIASource(dataLoader()));
        return verifier;
    }

    public static SignedDocumentValidator configureDocumentValidator(SignedDocumentValidator validator) {
        // DSS's analyzer also lazily creates a native loader for signature policies.
        var provider = new SignaturePolicyProvider();
        provider.setDataLoader(dataLoader());
        validator.setSignaturePolicyProvider(provider);
        return validator;
    }

    public static HttpClient.Builder httpClientBuilder() {
        return httpClientBuilder(configuration());
    }

    static HttpClient.Builder httpClientBuilder(ProxyConfiguration configuration) {
        var builder = HttpClient.newBuilder().sslContext(SystemTrust.sslContext());
        if (configuration.isConfigured())
            builder.proxy(ProxySelector.of(new InetSocketAddress(configuration.host(), configuration.port())));
        return builder;
    }

    /**
     * Aborts each HTTP request whose server does not start answering within the grace period, or which downloads
     * slower than the minimum average speed after it. DSS's timeouts only limit the connection and each read, so a
     * server that sends a few bytes at a time can take minutes, while a big file over a slow connection is fine.
     */
    static class SlowDownloadAbortingDataLoader extends CommonsDataLoader {
        private static final Duration CHECK_INTERVAL = Duration.ofSeconds(1);
        /** Its own thread, aborting closes sockets. */
        private static final ScheduledThreadPoolExecutor CHECKS = new ScheduledThreadPoolExecutor(1,
                Thread.ofPlatform().name("slow-download-checks").daemon().factory());

        static {
            CHECKS.setRemoveOnCancelPolicy(true);
        }

        private final Duration grace;
        private final long minBytesPerSecond;
        /** Body bytes received by the request on this thread so far, -1 until the server answers. */
        private final ThreadLocal<AtomicLong> received = new ThreadLocal<>();

        SlowDownloadAbortingDataLoader(Duration grace, long minBytesPerSecond) {
            this.grace = Objects.requireNonNull(grace);
            this.minBytesPerSecond = minBytesPerSecond;
            setHttpClientResponseHandler(new CommonsHttpClientResponseHandler() {
                @Override
                protected byte[] getContent(HttpEntity responseEntity) throws IOException {
                    var counter = received.get();
                    if (counter == null)
                        return super.getContent(responseEntity);

                    counter.set(0);
                    try (var content = new CountingInputStream(responseEntity.getContent(), counter)) {
                        return DSSUtils.toByteArray(content);
                    }
                }
            });
        }

        @Override
        protected ResponseEnvelope executeHttpRequest(CloseableHttpClient client, HttpUriRequest httpRequest,
                boolean includeResponseDetails, boolean includeResponseBody) throws IOException {
            var counter = new AtomicLong(-1);
            var started = System.nanoTime();
            received.set(counter);
            var check = CHECKS.scheduleWithFixedDelay(() -> {
                if (isTooSlow(counter.get(), Duration.ofNanos(System.nanoTime() - started)))
                    httpRequest.abort();
            }, grace.toMillis(), CHECK_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
            try {
                return super.executeHttpRequest(client, httpRequest, includeResponseDetails, includeResponseBody);
            } catch (IOException | RuntimeException e) {
                // DSS wraps failures while reading the body in DSSException
                if (httpRequest.isAborted())
                    throw new IOException("Aborted, no answer within " + grace.toSeconds() + " s or slower than "
                            + minBytesPerSecond + " B/s", e);
                throw e;
            } finally {
                check.cancel(false);
                received.remove();
            }
        }

        private boolean isTooSlow(long receivedBytes, Duration elapsed) {
            return elapsed.compareTo(grace) >= 0
                    && (receivedBytes < 0 || receivedBytes < minBytesPerSecond * elapsed.toMillis() / 1000);
        }
    }

    private static class CountingInputStream extends FilterInputStream {
        private final AtomicLong count;

        CountingInputStream(InputStream in, AtomicLong count) {
            super(in);
            this.count = count;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0)
                count.incrementAndGet();
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = super.read(buffer, offset, length);
            if (n > 0)
                count.addAndGet(n);
            return n;
        }
    }

    /** Only HTTP(S) goes through the proxy, other schemes (e.g. LDAP) would connect directly. */
    private record HttpOnlyDataLoader(DataLoader delegate) implements DataLoader {
        // Prefix check only: URLs from certificates (e.g. LDAP DNs with spaces) need not be valid URIs.
        private static boolean isAllowed(String url) {
            var lowerCaseUrl = url.toLowerCase(Locale.ROOT);
            return lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://")
                    || lowerCaseUrl.startsWith("file:");
        }

        private static void check(String url) {
            if (!isAllowed(url))
                throw new DSSExternalResourceException("Outbound request blocked by proxy configuration: " + url);
        }

        @Override
        public byte[] get(String url) {
            check(url);
            return delegate.get(url);
        }

        @Override
        public DataAndUrl get(List<String> urls) {
            // Filter before delegating: the delegate's list method calls its own get().
            var allowed = urls.stream().filter(HttpOnlyDataLoader::isAllowed).toList();
            if (allowed.isEmpty())
                throw new DSSExternalResourceException("Outbound requests blocked by proxy configuration.");
            return delegate.get(allowed);
        }

        @Override
        public byte[] post(String url, byte[] content) {
            check(url);
            return delegate.post(url, content);
        }

        @Override
        public void setContentType(String contentType) {
            delegate.setContentType(contentType);
        }
    }
}
