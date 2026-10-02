package digital.slovensko.autogram.core;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/** Loopback-only forwarding proxy and destinations, with independently recorded connections. */
class LocalProxyFixture implements AutoCloseable {
    private final ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor();
    private final ServerSocket proxy = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
    private final ServerSocket forbidden = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
    private final HttpServer http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    private final HttpsServer https = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    final List<String> requests = new CopyOnWriteArrayList<>();
    final List<String> contentTypes = new CopyOnWriteArrayList<>();
    final AtomicInteger destinationRequests = new AtomicInteger();
    final AtomicInteger forbiddenConnections = new AtomicInteger();
    final SSLContext tls;
    final Path keyStorePath;
    final X509Certificate certificate;
    volatile boolean requireAuthentication;

    LocalProxyFixture(Path directory) throws Exception {
        keyStorePath = directory.resolve("localhost.p12");
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", "localhost", "-keyalg", "RSA", "-storetype", "PKCS12",
                "-keystore", keyStorePath.toString(), "-storepass", "changeit", "-keypass", "changeit",
                "-dname", "CN=localhost", "-ext", "SAN=dns:localhost,ip:127.0.0.1", "-validity", "2",
                "-ext", "AIA=caIssuers:URI:" + httpUrl("/issuer") + ",ocsp:URI:" + httpUrl("/ocsp"),
                "-ext", "CRL=URI:" + httpUrl("/crl"))
                .redirectErrorStream(true).start();
        var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0)
            throw new IOException(output);
        var store = KeyStore.getInstance("PKCS12");
        try (var input = new FileInputStream(keyStorePath.toFile())) {
            store.load(input, "changeit".toCharArray());
        }
        certificate = (X509Certificate) store.getCertificate("localhost");
        var issuerBytes = certificate.getEncoded();
        var keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keys.init(store, "changeit".toCharArray());
        var trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(store);
        tls = SSLContext.getInstance("TLS");
        tls.init(keys.getKeyManagers(), trust.getTrustManagers(), null);
        https.setHttpsConfigurator(new HttpsConfigurator(tls));
        for (var server : List.of(http, https)) {
            server.setExecutor(threads);
            server.createContext("/", exchange -> {
                destinationRequests.incrementAndGet();
                contentTypes.add(String.valueOf(exchange.getRequestHeaders().getFirst("Content-Type")));
                exchange.getRequestBody().readAllBytes();
                if (exchange.getRequestURI().getPath().startsWith("/redirect")) {
                    exchange.getResponseHeaders().set("Location", exchange.getRequestURI().getPath().equals("/redirect-https")
                            ? httpsUrl("/resource") : "/resource");
                    exchange.sendResponseHeaders(302, -1);
                } else {
                    var body = switch (exchange.getRequestURI().getPath()) {
                        case "/issuer" -> issuerBytes;
                        case "/update-json" -> "{\"tag_name\":\"v999.0.0\"}".getBytes(StandardCharsets.UTF_8);
                        default -> "fixture-resource".getBytes(StandardCharsets.UTF_8);
                    };
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                }
                exchange.close();
            });
            server.start();
        }
        threads.submit(() -> {
            while (!forbidden.isClosed()) {
                try (var ignored = forbidden.accept()) {
                    forbiddenConnections.incrementAndGet();
                } catch (IOException e) {
                    if (!forbidden.isClosed())
                        throw new UncheckedIOException(e);
                }
            }
        });
        threads.submit(() -> {
            while (!proxy.isClosed()) {
                try {
                    var socket = proxy.accept();
                    threads.submit(() -> forward(socket));
                } catch (IOException e) {
                    if (!proxy.isClosed())
                        throw new UncheckedIOException(e);
                }
            }
        });
    }

    String proxyUrl() { return "http://127.0.0.1:" + proxy.getLocalPort(); }
    String httpUrl(String path) { return "http://127.0.0.1:" + http.getAddress().getPort() + path; }
    String httpsUrl(String path) { return "https://localhost:" + https.getAddress().getPort() + path; }
    String forbiddenUrl(String scheme) { return scheme + "://127.0.0.1:" + forbidden.getLocalPort() + "/resource"; }

    CommonsDataLoader trustedLoader() {
        var loader = new CommonsDataLoader();
        loader.setSslTruststore(new FileDocument(keyStorePath.toFile()));
        loader.setSslTruststorePassword("changeit".toCharArray());
        loader.setSslTruststoreType("PKCS12");
        return loader;
    }

    private void forward(Socket client) {
        try (client) {
            client.setSoTimeout(10000);
            var input = client.getInputStream();
            var header = new ByteArrayOutputStream();
            int c;
            while ((c = input.read()) != -1) {
                header.write(c);
                var bytes = header.toByteArray();
                int n = bytes.length;
                if (n >= 4 && bytes[n - 4] == '\r' && bytes[n - 3] == '\n'
                        && bytes[n - 2] == '\r' && bytes[n - 1] == '\n')
                    break;
            }
            var text = header.toString(StandardCharsets.ISO_8859_1);
            var lines = text.split("\r\n");
            var first = lines[0].split(" ");
            requests.add(lines[0]);
            if (requireAuthentication) {
                client.getOutputStream().write("""
                        HTTP/1.1 407 Proxy Authentication Required\r
                        Proxy-Authenticate: Basic realm=fixture\r
                        Content-Length: 0\r
                        Connection: close\r
                        \r
                        """.getBytes(StandardCharsets.ISO_8859_1));
                return;
            }
            boolean connect = first[0].equals("CONNECT");
            var uri = URI.create(connect ? "http://" + first[1] : first[1]);
            // Never allow the fixture to connect outside loopback.
            if (!(uri.getHost().equals("localhost") || uri.getHost().equals("127.0.0.1")))
                throw new IOException("Non-local fixture destination");
            try (var destination = new Socket(uri.getHost(), uri.getPort())) {
                destination.setSoTimeout(10000);
                if (connect) {
                    client.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n"
                            .getBytes(StandardCharsets.ISO_8859_1));
                } else {
                    var path = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
                    var rewritten = new StringBuilder(first[0] + " " + path + " " + first[2] + "\r\n");
                    for (int i = 1; i < lines.length; i++) {
                        if (!lines[i].toLowerCase().startsWith("connection:")
                                && !lines[i].toLowerCase().startsWith("proxy-connection:"))
                            rewritten.append(lines[i]).append("\r\n");
                    }
                    rewritten.append("Connection: close\r\n\r\n");
                    destination.getOutputStream().write(rewritten.toString().getBytes(StandardCharsets.ISO_8859_1));
                }
                var upload = threads.submit(() -> {
                    try {
                        input.transferTo(destination.getOutputStream());
                    } catch (IOException ignored) { }
                });
                destination.getInputStream().transferTo(client.getOutputStream());
                upload.cancel(true);
            }
        } catch (IOException ignored) {
            // Client closes tunnels once its response is complete.
        }
    }

    void stopProxy() throws IOException { proxy.close(); }

    @Override
    public void close() throws IOException {
        proxy.close();
        forbidden.close();
        http.stop(0);
        https.stop(0);
        threads.shutdownNow();
    }
}
