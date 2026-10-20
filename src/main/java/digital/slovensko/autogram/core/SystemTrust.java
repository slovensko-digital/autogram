package digital.slovensko.autogram.core;

import digital.slovensko.autogram.util.OperatingSystem;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * TLS trust for Autogram's network clients: CAs trusted by the JDK and by the operating system, e.g. a company CA
 * of a proxy that inspects HTTPS.
 */
final class SystemTrust {
    private static final Logger logger = LoggerFactory.getLogger(SystemTrust.class);
    private static final List<Path> LINUX_CA_BUNDLES = List.of(
            Path.of("/etc/ssl/certs/ca-certificates.crt"), // Debian, Ubuntu, Arch
            Path.of("/etc/pki/tls/certs/ca-bundle.crt"), // Fedora, RHEL
            Path.of("/etc/ssl/ca-bundle.pem"), // openSUSE
            Path.of("/etc/ssl/cert.pem")); // Alpine

    private static class Holder {
        static final X509TrustManager TRUST_MANAGER = trustManager(systemCertificates());
        static final SSLContext SSL_CONTEXT = sslContext(TRUST_MANAGER);
    }

    private SystemTrust() { }

    /** For DSS data loaders, which otherwise trust only the JDK's CAs. */
    static TrustStrategy trustStrategy() {
        return trustStrategy(Holder.TRUST_MANAGER);
    }

    /** For java.net.http clients. */
    static SSLContext sslContext() {
        return Holder.SSL_CONTEXT;
    }

    static TrustStrategy trustStrategy(X509TrustManager trustManager) {
        return (chain, authType) -> {
            try {
                trustManager.checkServerTrusted(chain, authType);
                return true;
            } catch (CertificateException e) {
                return false;
            }
        };
    }

    static SSLContext sslContext(X509TrustManager trustManager) {
        try {
            var context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{trustManager}, null);
            return context;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    static X509TrustManager trustManager(Collection<X509Certificate> systemCertificates) {
        try {
            var certificates = new LinkedHashSet<X509Certificate>(List.of(x509TrustManager(null).getAcceptedIssuers()));
            certificates.addAll(systemCertificates);
            var store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(null, null);
            var index = 0;
            for (var certificate : certificates)
                store.setCertificateEntry(String.valueOf(index++), certificate);

            return x509TrustManager(store);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static List<X509Certificate> systemCertificates() {
        return switch (OperatingSystem.current()) {
            case WINDOWS -> keyStoreCertificates("Windows-ROOT");
            case MAC -> {
                // System roots, and certificates trusted by the user or an administrator (e.g. via MDM)
                var certificates = new ArrayList<>(keyStoreCertificates("KeychainStore-ROOT"));
                certificates.addAll(keyStoreCertificates("KeychainStore"));
                yield certificates;
            }
            case LINUX -> bundleCertificates();
        };
    }

    /** With a null store, the JDK's default trusted CAs (cacerts or javax.net.ssl.trustStore). */
    private static X509TrustManager x509TrustManager(KeyStore store) throws GeneralSecurityException {
        var factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store);
        for (var trustManager : factory.getTrustManagers())
            if (trustManager instanceof X509TrustManager x509TrustManager)
                return x509TrustManager;

        throw new GeneralSecurityException("No X509TrustManager");
    }

    private static List<X509Certificate> keyStoreCertificates(String type) {
        try {
            var store = KeyStore.getInstance(type);
            store.load(null, null);
            var certificates = new ArrayList<X509Certificate>();
            for (var alias : Collections.list(store.aliases()))
                if (store.isCertificateEntry(alias) && store.getCertificate(alias) instanceof X509Certificate certificate)
                    certificates.add(certificate);

            return certificates;
        } catch (GeneralSecurityException | IOException e) {
            logger.warn("Cannot load system certificates from {}", type, e);
            return List.of();
        }
    }

    private static List<X509Certificate> bundleCertificates() {
        for (var bundle : LINUX_CA_BUNDLES) {
            if (!Files.isReadable(bundle))
                continue;

            try (var input = Files.newInputStream(bundle)) {
                return CertificateFactory.getInstance("X.509").generateCertificates(input).stream()
                        .map(X509Certificate.class::cast).toList();
            } catch (GeneralSecurityException | IOException e) {
                logger.warn("Cannot load system certificates from {}", bundle, e);
            }
        }
        return List.of();
    }
}
