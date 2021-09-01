package com.octosign.whitelabel.communication.document;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * XML document for signing
 */
public class XMLDocument extends Document {

    public static final String MIME_TYPE = "application/xml";

    protected String transformation;
    protected String schema;

    public XMLDocument() { }

    public XMLDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
    }

    public XMLDocument(Document document, String schema, String transformation) {
        this(document);
        this.schema = schema;
        this.transformation = transformation;
    }

    public String getSchema() { return this.schema; }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTransformation() {
        return this.transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    /**
     * Apply defined transformation on the document and get it
     *
     * @return String with the transformed XML document - for example its HTML representation
     */
    public String getTransformed() {
        if (content == null || transformation == null)
            throw new RuntimeException("Document has no content or transformation defined");

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(content));
        var xmlOutWriter = new StringWriter();

        var transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer(xslSource);
            transformer.transform(xmlInSource, new StreamResult(xmlOutWriter));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Transformation initialization error: Unable to set requested feature | Unable to parse XML source or perform initialization", e);
        } catch (TransformerException e) {
            throw new RuntimeException("Transformation aborted: unknown transformation error.", e);
        }

        return xmlOutWriter.toString();
    }

    public void validate() {
        if (content == null || schema == null)
            throw new RuntimeException("Document has no content or schema defined");

        var xsdSource = new StreamSource(new StringReader(schema));
        var xmlInSource = new StreamSource(new StringReader(content));

        try {
            var schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource);
            schema.newValidator().validate(xmlInSource);
        } catch (SAXException e) {
            throw new RuntimeException("Corrupted or invalid XSD schema", e);
        } catch (IOException e) {
            throw new RuntimeException("Validation failed - incorrect XML format", e);
        }
    }
}
