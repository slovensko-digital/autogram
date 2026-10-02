package digital.slovensko.autogram.core;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.client.http.NativeHTTPDataLoader;
import eu.europa.esig.dss.spi.policy.SignaturePolicyProvider;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.validation.SignedDocumentValidator;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;

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
        return startup == null ? ProxyConfiguration.parse(null) : startup;
    }

    public static DataLoader dataLoader() {
        return configure(new CommonsDataLoader(), configuration());
    }

    public static DataLoader timestampDataLoader() {
        return configure(new TimestampDataLoader(), configuration());
    }

    public static DataLoader ocspDataLoader() {
        return configure(new OCSPDataLoader(), configuration());
    }

    public static FileCacheDataLoader fileCacheDataLoader(long expirationTime) {
        var cache = new FileCacheDataLoader();
        cache.setCacheExpirationTime(expirationTime);
        cache.setDataLoader(dataLoader());
        return cache;
    }

    static DataLoader configure(CommonsDataLoader loader, ProxyConfiguration configuration) {
        if (configuration.state() == ProxyConfiguration.State.UNCONFIGURED)
            return loader;
        if (configuration.state() == ProxyConfiguration.State.CONFIGURED) {
            var properties = new ProxyProperties();
            properties.setScheme("http");
            properties.setHost(configuration.host());
            properties.setPort(configuration.port());
            var proxy = new ProxyConfig();
            proxy.setHttpProperties(properties);
            proxy.setHttpsProperties(properties);
            loader.setProxyConfig(proxy);
        }
        return new RestrictedDataLoader(loader, configuration);
    }

    public static CommonCertificateVerifier certificateVerifier() {
        var verifier = new CommonCertificateVerifier();
        // DSS's default AIA loader is NativeHTTPDataLoader, not CommonsDataLoader.
        if (configuration().state() != ProxyConfiguration.State.UNCONFIGURED)
            verifier.setAIASource(new DefaultAIASource(dataLoader()));
        return verifier;
    }

    public static SignaturePolicyProvider signaturePolicyProvider() {
        var provider = new SignaturePolicyProvider();
        provider.setDataLoader(configuration().state() == ProxyConfiguration.State.UNCONFIGURED
                ? new NativeHTTPDataLoader() : dataLoader());
        return provider;
    }

    public static SignedDocumentValidator configureDocumentValidator(SignedDocumentValidator validator) {
        // DSS's analyzer also lazily creates a native loader for signature policies.
        if (configuration().state() != ProxyConfiguration.State.UNCONFIGURED)
            validator.setSignaturePolicyProvider(signaturePolicyProvider());
        return validator;
    }

    public static HttpClient.Builder httpClientBuilder() {
        return httpClientBuilder(configuration());
    }

    static HttpClient.Builder httpClientBuilder(ProxyConfiguration configuration) {
        var builder = HttpClient.newBuilder();
        if (configuration.state() == ProxyConfiguration.State.UNCONFIGURED)
            return builder;
        return builder.proxy(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (configuration.state() == ProxyConfiguration.State.BLOCKED)
                    throw new IllegalStateException("Outbound networking blocked by invalid proxy configuration.");
                return List.of(new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(configuration.host(), configuration.port())));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress address, IOException error) {
                // Never retry directly.
            }
        });
    }

    private record RestrictedDataLoader(DataLoader delegate, ProxyConfiguration configuration) implements DataLoader {
        private void check(String url) {
            var scheme = URI.create(url).getScheme();
            if ("file".equalsIgnoreCase(scheme))
                return;
            if (configuration.state() == ProxyConfiguration.State.BLOCKED
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)))
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
            var allowed = urls.stream().filter(url -> {
                try {
                    check(url);
                    return true;
                } catch (DSSExternalResourceException e) {
                    return false;
                }
            }).toList();
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
