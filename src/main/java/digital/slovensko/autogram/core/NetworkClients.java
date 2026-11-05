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
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

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
        return publicOnly(new CheckedRedirectsDataLoader(url -> isHttp(url) && !leadsToInternalAddress(url)));
    }

    public static DataLoader timestampDataLoader() {
        return configure(trustingSystem(new TimestampDataLoader()), configuration());
    }

    public static DataLoader ocspDataLoader() {
        var loader = new OCSPDataLoader();
        loader.setRedirectsEnabled(false);
        return publicOnly(loader);
    }

    private static DataLoader publicOnly(CommonsDataLoader loader) {
        return new RestrictedDataLoader(configure(trustingSystem(loader), configuration()),
                url -> !leadsToInternalAddress(url));
    }

    private static CommonsDataLoader trustingSystem(CommonsDataLoader loader) {
        loader.setTrustStrategy(SystemTrust.trustStrategy());
        return loader;
    }

    // Prefix check only: URLs from certificates (e.g. LDAP DNs with spaces) need not be valid URIs.
    private static boolean isHttp(String url) {
        var lowerCaseUrl = url.toLowerCase(Locale.ROOT);
        return lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://");
    }

    private static boolean leadsToInternalAddress(String url) {
        try {
            var host = new URI(url).getHost();
            return host == null || Arrays.stream(InetAddress.getAllByName(host)).anyMatch(NetworkClients::isInternal);
        } catch (URISyntaxException e) {
            return true;
        } catch (UnknownHostException e) {
            return false; // only a proxy may know the host, otherwise connecting fails anyway
        }
    }

    private static boolean isInternal(InetAddress address) {
        var bytes = address.getAddress();
        var sharedIpv4 = bytes.length == 4 && (bytes[0] & 0xff) == 100 && (bytes[1] & 0xc0) == 64;
        var uniqueLocalIpv6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
        return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || sharedIpv4 || uniqueLocalIpv6;
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
            return new RestrictedDataLoader(loader, NetworkClients::isHttp);

        var properties = new ProxyProperties();
        properties.setScheme("http");
        properties.setHost(configuration.host());
        properties.setPort(configuration.port());
        var proxy = new ProxyConfig();
        proxy.setHttpProperties(properties);
        proxy.setHttpsProperties(properties);
        loader.setProxyConfig(proxy);
        return new RestrictedDataLoader(loader, NetworkClients::isHttp);
    }

    public static CommonCertificateVerifier certificateVerifier() {
        var verifier = new CommonCertificateVerifier();
        // DSS's default AIA loader is NativeHTTPDataLoader, not CommonsDataLoader.
        verifier.setAIASource(new DefaultAIASource(dataLoader()));
        return verifier;
    }

    public static SignaturePolicyProvider signaturePolicyProvider() {
        // DSS's analyzer also lazily creates a native loader for signature policies.
        var provider = new SignaturePolicyProvider();
        provider.setDataLoader(dataLoader());
        return provider;
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

    private static final class CheckedRedirectsDataLoader extends CommonsDataLoader {
        private final Predicate<String> isAllowed;

        CheckedRedirectsDataLoader(Predicate<String> isAllowed) {
            this.isAllowed = isAllowed;
        }

        @Override
        protected synchronized HttpClientBuilder getHttpClientBuilder(String url) {
            return super.getHttpClientBuilder(url).setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException {
                    var location = super.getLocationURI(request, response, context);
                    if (!isAllowed.test(location.toString()))
                        throw new ProtocolException("Redirect blocked: " + location);
                    return location;
                }
            });
        }
    }

    private record RestrictedDataLoader(DataLoader delegate, Predicate<String> isAllowed) implements DataLoader {
        private void check(String url) {
            if (!isAllowed.test(url))
                throw new DSSExternalResourceException("Outbound request blocked: " + url);
        }

        @Override
        public byte[] get(String url) {
            check(url);
            return delegate.get(url);
        }

        @Override
        public DataAndUrl get(List<String> urls) {
            // Filter before delegating: the delegate's list method calls its own get().
            var allowed = urls.stream().filter(isAllowed).toList();
            if (allowed.isEmpty())
                throw new DSSExternalResourceException("Outbound requests blocked.");
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
