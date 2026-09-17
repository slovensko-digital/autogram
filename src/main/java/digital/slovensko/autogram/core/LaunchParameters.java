package digital.slovensko.autogram.core;

import java.util.*;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import javafx.application.Application.Parameters;

import static digital.slovensko.autogram.core.Configuration.getProperty;
import static digital.slovensko.autogram.core.LaunchParameters.Validations.*;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class LaunchParameters {
    private static final String AUTOGRAM_URL_PREFIX = "autogram://";
    private static final String FILE_URI_PREFIX = "file://";

    protected Map<String, String> parameters;
    private String protocol;
    private String host;
    private int port;
    private String origin;
    private String secretKey;
    private int initialNonce;
    private String language;
    private boolean standaloneMode;
    private final List<File> files;

    private LaunchParameters(Map<String, String> params, boolean standaloneMode, List<File> files) {
        this.parameters = params;
        this.files = files;

        this.standaloneMode = standaloneMode;

        var protocol = ofNullable(params.get("protocol")).orElse(getProperty("server.defaultProtocol"));
        var host = ofNullable(params.get("host")).orElse(getProperty("server.defaultAddress"));
        var port = ofNullable(params.get("port")).orElse(getProperty("server.defaultPort"));
        var origin = ofNullable(params.get("origin")).orElse(getProperty("server.defaultOrigin"));
        var language = ofNullable(params.get("language")).orElse(Locale.getDefault().getLanguage());
        var key = params.get("key");
        var nonce = params.get("nonce");


        this.protocol = validateProtocol(protocol);
        this.host = validateHost(host);
        this.port = validatePort(port);
        this.origin = validateOrigin(origin);
        this.language = validateLanguage(language);

        if ((key != null) && !key.isBlank())
            this.secretKey = validateSecretKey(key);
        if ((nonce != null) && !nonce.isBlank())
            this.initialNonce = validateInitialNonce(nonce);
    }

    public static LaunchParameters fromParameters(Parameters parameters) {
        var named = parameters.getNamed();
        var unnamed = parameters.getUnnamed();
        var urlParam = named.get("url");
        if (urlParam == null)
            urlParam = unnamed.stream().filter(LaunchParameters::isAutogramUrl).findFirst().orElse(null);

        try {
            Map<String, String> params;
            if (urlParam != null) {
                var url = new URIBuilder(urlParam);
                params = getUrlQueryParameters(url.getQueryParams());
            } else {
                params = named;
            }

            return new LaunchParameters(params, urlParam == null || urlParam.isBlank(), filesFrom(unnamed));

        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // TODO: handle exception
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO: handle exception
        }
    }

    public static boolean isAutogramUrl(String arg) {
        return arg.startsWith(AUTOGRAM_URL_PREFIX);
    }

    /**
     * Picks the existing files out of unnamed launch arguments, which may also
     * contain an autogram:// URL or stray flags that JavaFX does not treat as named.
     */
    public static List<File> filesFrom(List<String> args) {
        return args.stream()
                .filter(arg -> !isAutogramUrl(arg))
                .map(LaunchParameters::toFile)
                .filter(File::isFile)
                .toList();
    }

    private static File toFile(String arg) {
        if (!arg.startsWith(FILE_URI_PREFIX))
            return new File(arg);

        try {
            return Path.of(URI.create(arg)).toFile();
        } catch (IllegalArgumentException e) {
            return new File(arg.substring(FILE_URI_PREFIX.length()));
        }
    }

    public List<File> getFiles() {
        return files;
    }

    private static Map<String, String> getUrlQueryParameters(List<NameValuePair> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(queryParams.stream()
                    .collect(toMap(NameValuePair::getName, NameValuePair::getValue)));
        }
    }

    public boolean isStandaloneMode() {
        return standaloneMode;
    }

    public boolean isRequiredSSL() {
        return protocol.equalsIgnoreCase("https");
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getOrigin() {
        return origin;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public int getInitialNonce() {
        return initialNonce;
    }

    public String getLanguage() {
        return language;
    }

    public static class Validations {
        private static final int MAX_PORT_NUMBER = 65535;
        private static final String VALID_ORIGIN_REGEX = "^\\*|((https?:\\/\\/)([^\\s.:/\\\\]+[\\.])*([^\\s.:/\\\\]+)(:\\d+)?)$";
        private static final String VALID_HOSTNAME_REGEX = "^(([a-z]|[A-Z]|[0-9]|[\u00a0-\uffff]|-)+|loopback\\.autogram\\.slovensko\\.digital)$";
        private static final String VALID_LANGUAGE_REGEX = "^([a-z]|[A-Z]|[0-9]|_|-)+$";

        public static String validateProtocol(String protocol) {
            if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https"))
                return protocol.toLowerCase(Locale.ROOT);

            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }

        public static String validateHost(String host) {
            if (Pattern.compile(VALID_HOSTNAME_REGEX).matcher(host).matches())
                return host;
            else
                throw new IllegalArgumentException("Invalid hostname " + host);
        }

        public static int validatePort(String input) {
            int port = validInteger(input);

            if (port <= MAX_PORT_NUMBER && port >= 1)
                return port;
            else
                throw new IllegalArgumentException("Port " + input + " is outside the allowed range (1-65535)");
        }

        public static String validateOrigin(String origin) {
            if (Pattern.compile(VALID_ORIGIN_REGEX).matcher(origin).matches())
                return origin;
            else
                throw new IllegalArgumentException("Origin " + origin + " is invalid.");
        }

        public static String validateSecretKey(String key) {
            if (key.length() < 16) {
                throw new IllegalArgumentException("Secret key is too short. Minimum length is 16 characters.");
            }

            return key;
        }

        public static int validateInitialNonce(String nonce) {
            return validInteger(nonce);
        }

        public static String validateLanguage(String language) {
            if (Pattern.compile(VALID_LANGUAGE_REGEX).matcher(language).matches()) {
                return language.toLowerCase().strip()
                        .replaceAll("[_/,.;+\\\\]", "-")
                        .replaceFirst("en-[a-z]{2}", "en-us");

            } else
                throw new IllegalArgumentException("Language " + language + " is not valid.");
        }

        private static int validInteger(String input) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Input [" + input + "] is not a valid integer value. Details: " + e.getMessage());
            }
        }
    }

    public boolean isProtocolHttps() {
        return protocol.equalsIgnoreCase("https");
    }
}
