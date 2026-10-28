package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.http.ResponseEnvelope;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.policy.SignaturePolicyProvider;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    /** Each download that takes longer than the time limit fails. */
    public static FileCacheDataLoader fileCacheDataLoader(long expirationTime, Duration timeLimit) {
        return fileCacheDataLoader(expirationTime,
                configure(trustingSystem(new TimeLimitedDataLoader(timeLimit)), configuration()));
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
     * Aborts each HTTP request that takes longer than the time limit. DSS's timeouts only limit the connection and each
     * read, so a server that sends a few bytes at a time can take minutes.
     */
    static class TimeLimitedDataLoader extends CommonsDataLoader {
        private final Duration timeLimit;

        TimeLimitedDataLoader(Duration timeLimit) {
            this.timeLimit = Objects.requireNonNull(timeLimit);
        }

        @Override
        protected ResponseEnvelope executeHttpRequest(CloseableHttpClient client, HttpUriRequest httpRequest,
                boolean includeResponseDetails, boolean includeResponseBody) throws IOException {
            // Runs on the delay scheduler itself, the common pool can be busy with blocking checks. Cancelling it
            // before the time limit keeps the request from being aborted.
            var abort = CompletableFuture.runAsync(httpRequest::abort,
                    CompletableFuture.delayedExecutor(timeLimit.toMillis(), TimeUnit.MILLISECONDS, Runnable::run));
            try {
                return super.executeHttpRequest(client, httpRequest, includeResponseDetails, includeResponseBody);
            } catch (IOException | RuntimeException e) {
                // DSS wraps failures while reading the body in DSSException
                if (httpRequest.isAborted())
                    throw new IOException("Aborted after the time limit of " + timeLimit.toMillis() + " ms", e);
                throw e;
            } finally {
                abort.cancel(false);
            }
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
