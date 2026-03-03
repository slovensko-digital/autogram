package digital.slovensko.autogram.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class XMLUtils {
    public static DocumentBuilder getSecureDocumentBuilder() throws ParserConfigurationException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        return builderFactory.newDocumentBuilder();
    }

    public static TransformerFactory getSecureTransformerFactory() throws TransformerConfigurationException {
        var transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        return transformerFactory;
    }

    public static SchemaFactory getSecureSchemaFactory() throws SAXNotRecognizedException, SAXNotSupportedException {
        var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        return schemaFactory;
    }
}
