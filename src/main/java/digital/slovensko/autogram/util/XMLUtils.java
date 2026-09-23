package digital.slovensko.autogram.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class XMLUtils {
    public static DocumentBuilder getSecureDocumentBuilder() throws ParserConfigurationException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setXIncludeAware(false);
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        return builderFactory.newDocumentBuilder();
    }

    public static TransformerFactory getSecureTransformerFactory() throws TransformerConfigurationException {
        var transformerFactory = new TransformerFactoryImpl();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        // Stylesheets may come from untrusted requests. The JAXP attributes above only restrict Saxon's resource
        // resolver, but collection() and uri-collection() bypass it, so deny all protocols globally and disable
        // collections altogether.
        var configuration = transformerFactory.getConfiguration();
        configuration.setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, "");
        // FEATURE_SECURE_PROCESSING already implies this (and with it disables xsl:result-document), set it
        // explicitly so the protection does not hinge on a single flag
        configuration.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        configuration.setConfigurationProperty(Feature.DISABLE_XSL_EVALUATE, true);
        configuration.setCollectionFinder((context, collectionURI) -> {
            throw new XPathException("Access to collection " + collectionURI + " is not allowed", "FODC0002");
        });

        return transformerFactory;
    }

    public static SchemaFactory getSecureSchemaFactory() throws SAXNotRecognizedException, SAXNotSupportedException {
        var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        schemaFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        return schemaFactory;
    }
}
