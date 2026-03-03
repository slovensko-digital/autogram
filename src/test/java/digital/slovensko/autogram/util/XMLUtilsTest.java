package digital.slovensko.autogram.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class XMLUtilsTest {
    @BeforeAll
    public static void setup() throws Exception {
        Files.writeString(Path.of("/tmp/test.txt"), "supersecret");
    }

    @AfterAll
    public static void cleanup() throws Exception {
        Files.deleteIfExists(Path.of("/tmp/test.txt"));
    }

    @Test
    public void testGetSecureDocumentBuilder() throws Exception {
        var builder = XMLUtils.getSecureDocumentBuilder();
        assertNotNull(builder);
    }

    @Test
    public void testGetSecureTransformerFactory() throws Exception {
        var factory = XMLUtils.getSecureTransformerFactory();
        assertNotNull(factory);
    }

    @Test
    public void testGetSecureSchemaFactory() throws Exception {
        var factory = XMLUtils.getSecureSchemaFactory();
        assertNotNull(factory);
    }

    @Test
    public void testBaseDocumentBuilderUnsecure() throws Exception {
        var builderFactory = DocumentBuilderFactory.newInstance();
        var builder = builderFactory.newDocumentBuilder();
        var xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///tmp/test.txt\">]><foo>&xxe;</foo>";
        var document = builder.parse(new InputSource(new StringReader(xml)));
        var content = document.getDocumentElement().getTextContent();
        assertNotNull(content);
        assertTrue(content.contains("supersecret"));
    }

    @Test
    public void testGetSecureDocumentBuilderSecurity() throws Exception {
        var builder = XMLUtils.getSecureDocumentBuilder();
        var xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///tmp/test.txt\">]><foo>&xxe;</foo>";
        assertThrows(Exception.class, () -> builder.parse(new InputSource(new StringReader(xml))));
    }

    @Test
    public void testBaseDocumentBuilderUnsecureWithExternalEntity() throws Exception {
        var builderFactory = DocumentBuilderFactory.newInstance();
        var builder = builderFactory.newDocumentBuilder();
        var xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"https://raw.githubusercontent.com/slovensko-digital/autogram/refs/heads/main/pom.xml\">]><foo>&xxe;</foo>";
        var document = builder.parse(new InputSource(new StringReader(xml)));
        var content = document.getDocumentElement().getTextContent();
        assertNotNull(content);
        assertTrue(content.contains("project"));
    }

    @Test
    public void testGetSecureDocumentBuilderSecurityWithExternalEntity() throws Exception {
        var builder = XMLUtils.getSecureDocumentBuilder();
        var xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"https://raw.githubusercontent.com/slovensko-digital/autogram/refs/heads/main/pom.xml\">]><foo>&xxe;</foo>";
        try {
            builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Access to external DTD") || e.getMessage().contains("DOCTYPE is disallowed"));
        }
    }

    @Test
    public void testGetSecureDocumentBuilderDoctypeDeclarationSecurity1() throws Exception {
        var builder = XMLUtils.getSecureDocumentBuilder();
        var xml = "<!DOCTYPE foo><foo>bar</foo>";
        try {
            builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed") || e.getMessage().contains("Access to external DTD"));
        }
    }

    @Test
    public void testGetSecureDocumentBuilderDoctypeDeclarationSecurity2() throws Exception {
        var builder = XMLUtils.getSecureDocumentBuilder();
        var xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///tmp/test.txt\">]><foo>&xxe;</foo>";
        try {
            builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed") || e.getMessage().contains("Access to external DTD"));
        }
    }

    @Test
    public void testGetSecureTransformerFactoryBlocksExternalStylesheet() throws Exception {
        var factory = XMLUtils.getSecureTransformerFactory();
        var xslt = "<?xml version=\"1.0\"?>"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:import href=\"file:///tmp/test.txt\"/>"
                + "<xsl:template match=\"/\"><out/></xsl:template>"
                + "</xsl:stylesheet>";
        assertThrows(Exception.class, () -> factory.newTransformer(new StreamSource(new StringReader(xslt))));
    }

    @Test
    public void testGetSecureTransformerFactoryBlocksExternalDTD() throws Exception {
        var factory = XMLUtils.getSecureTransformerFactory();
        var xslt = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE stylesheet SYSTEM \"file:///tmp/test.txt\">"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:template match=\"/\"><out/></xsl:template>"
                + "</xsl:stylesheet>";
        assertThrows(Exception.class, () -> factory.newTransformer(new StreamSource(new StringReader(xslt))));
    }

    @Test
    public void testGetSecureTransformerFactoryAllowsNormalTransformation() throws Exception {
        var factory = XMLUtils.getSecureTransformerFactory();
        var xslt = "<?xml version=\"1.0\"?>"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:template match=\"/\"><result><xsl:value-of select=\"/foo\"/></result></xsl:template>"
                + "</xsl:stylesheet>";
        var transformer = factory.newTransformer(new StreamSource(new StringReader(xslt)));
        var xml = "<foo>hello</foo>";
        var writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
        assertTrue(writer.toString().contains("hello"));
    }

    @Test
    public void testGetSecureSchemaFactoryBlocksExternalSchema() throws Exception {
        var factory = XMLUtils.getSecureSchemaFactory();
        var xsd = "<?xml version=\"1.0\"?>"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "<xs:import namespace=\"urn:test\" schemaLocation=\"file:///tmp/test.txt\"/>"
                + "<xs:element name=\"foo\" type=\"xs:string\"/>"
                + "</xs:schema>";
        assertThrows(Exception.class, () ->
                factory.newSchema(new StreamSource(new StringReader(xsd))));
    }

    @Test
    public void testGetSecureSchemaFactoryBlocksExternalDTD() throws Exception {
        var factory = XMLUtils.getSecureSchemaFactory();
        var xsd = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE schema SYSTEM \"file:///tmp/test.txt\">"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "<xs:element name=\"foo\" type=\"xs:string\"/>"
                + "</xs:schema>";
        assertThrows(Exception.class, () ->
                factory.newSchema(new StreamSource(new StringReader(xsd))));
    }

    @Test
    public void testGetSecureSchemaFactoryAllowsValidSchema() throws Exception {
        var factory = XMLUtils.getSecureSchemaFactory();
        var xsd = "<?xml version=\"1.0\"?>"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "<xs:element name=\"foo\" type=\"xs:string\"/>"
                + "</xs:schema>";
        var schema = factory.newSchema(new StreamSource(new StringReader(xsd)));
        assertNotNull(schema);
    }
}
