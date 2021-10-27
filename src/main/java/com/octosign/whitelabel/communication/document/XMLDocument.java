package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.ui.IntegrationException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import com.octosign.whitelabel.communication.MimeType;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static com.octosign.whitelabel.ui.Main.getProperty;

/**
 * XML document for signing
 */
public class XMLDocument extends Document {

    public static final MimeType mimeType = MimeType.XML;

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
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    /**
     * Apply defined transformation on the document and get it
     *
     * @return String with the transformed XML document - for example its HTML representation
     */
    public String getTransformed() throws IntegrationException {
        if (content == null || transformation == null) {
            var missing = (content == null) ? "content" : "transformation";
            throw new IntegrationException(getProperty("error.missingContent", missing));
        }

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(content));
        var xmlOutWriter = new StringWriter();

        var transformerFactory = TransformerFactory.newInstance();

        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer(xslSource);
            transformer.transform(xmlInSource, new StreamResult(xmlOutWriter));
        } catch (TransformerConfigurationException e) {
            throw new IntegrationException(getProperty("error.transformationInitFailed", e));
        } catch (TransformerException e) {
            throw new IntegrationException(getProperty("error.transformationAborted", e));
        }

        return xmlOutWriter.toString();
    }

    public void validate() throws IntegrationException {
        if (content == null || schema == null) {
            var missing = (content == null) ? "content" : "schema";
            throw new IntegrationException(getProperty("error.missingContent", missing));
        }
        var xsdSource = new StreamSource(new StringReader(schema));
        var xmlInSource = new StreamSource(new StringReader(content));

        try {
            var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.newSchema(xsdSource).newValidator().validate(xmlInSource);
        } catch (SAXException e) {
            throw new IntegrationException(getProperty("error.corruptedSchemaFile", e));
        } catch (IOException e) {
            throw new IntegrationException(getProperty("error.invalidSchemaXmlFormat", e));
        }
    }
}
