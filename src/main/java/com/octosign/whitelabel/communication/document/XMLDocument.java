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

import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;

/**
 * XML document for signing
 */
public class XMLDocument extends Document {
    protected String transformation;
    protected String schema;

    public XMLDocument() { }

    public XMLDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
        setMimeType(document.getMimeType());
    }

    public XMLDocument(Document document, String schema, String transformation) {
        this(document);
        this.schema = schema;
        this.transformation = transformation;
    }

    public String getSchema() {
        return this.schema;
    }

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
    public String getTransformed() {
        if (isNullOrBlank(content) || isNullOrBlank(transformation)) {
            var attribute = isNullOrBlank(content) ? "body.parameters.content" : "body.parameters.transformation";
            throw new IntegrationException(Code.MISSING_INPUT, "Input attribute missing: " + attribute);
        }

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(content));
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

    public void validate() {
        if (isNullOrBlank(content) || isNullOrBlank(schema)) {
            var attribute = isNullOrBlank(content) ? "body.parameters.content" : "body.parameters.schema";
            throw new IntegrationException(Code.MISSING_INPUT, "Input attribute missing: " + attribute);
        }
        var xsdSource = new StreamSource(new StringReader(schema));
        Schema xsdSchema;

        try {
            xsdSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource);
        } catch (SAXException e) {
            throw new IntegrationException(Code.INVALID_SCHEMA, e);
        }

        var xmlInSource = new StreamSource(new StringReader(content));

        try {
            xsdSchema.newValidator().validate(xmlInSource);
        } catch (SAXException | IOException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        }
    }
}
