package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.error_handling.*;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static com.octosign.whitelabel.ui.utils.Utils.isNullOrBlank;

/**
 * XML document for signing
 */
public class XMLDocument extends Document {

    public XMLDocument(Document document) {
        super(document);
    }

    static {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    }

    public String getTransformed(String transformation) {
        if (isNullOrBlank(transformation))
            throw new IntegrationException(Code.MISSING_INPUT, "body.parameters.transformation missing!");

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(getContentString()));
        var xmlOutWriter = new StringWriter();
        var transformerFactory = TransformerFactory.newInstance();

        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer(xslSource);
            transformer.transform(xmlInSource, new StreamResult(xmlOutWriter));
        } catch (Exception e) {
            throw new IntegrationException(Code.TRANSFORMATION_ERROR, e);
        }

        return xmlOutWriter.toString();
    }

    public void validate(String schema) {
        if (isNullOrBlank(schema))
            throw new IntegrationException(Code.MISSING_INPUT, "body.parameters.schema missing!");

        var xsdSource = new StreamSource(new StringReader(schema));
        Schema xsdSchema;

        try {
            xsdSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource);
        } catch (SAXException e) {
            throw new IntegrationException(Code.INVALID_SCHEMA, e);
        }

        var xmlInSource = new StreamSource(new StringReader(getContentString()));

        try {
            xsdSchema.newValidator().validate(xmlInSource);
        } catch (SAXException | IOException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        }
    }
}
