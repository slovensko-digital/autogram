package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static com.octosign.whitelabel.ui.Main.translate;

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
            var missingAttribute = (content == null) ? "content" : "transformation";
            throw new IntegrationException(Code.ATTRIBUTE_MISSING, translate("error.missingContent", missingAttribute));
        }

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(content));
        var xmlOutWriter = new StringWriter();
        var transformerFactory = TransformerFactory.newInstance();

        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer(xslSource);
            transformer.transform(xmlInSource, new StreamResult(xmlOutWriter));
        } catch (TransformerException e) {
            throw new IntegrationException(Code.TRANSFORMATION_ERROR, translate("error.transformationFailed", e));
        }

        return xmlOutWriter.toString();
    }

    public void validate() throws IntegrationException {
//        if (content == null || schema == null) {
//            var missingAttribute = (content == null) ? "content" : "schema";
//            throw new IntegrationException(Code.ATTRIBUTE_MISSING, translate("error.missingContent", missingAttribute));
//        }
//        var xsdSource = new StreamSource(new StringReader(schema));
//        Schema xsdSchema;
//
//        try {
//            xsdSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource);
//        } catch (SAXException e) {
//            throw new IntegrationException(Code.INVALID_SCHEMA, translate("error.xsdSchemaInvalid", e));
//        }
//
//        var xmlInSource = new StreamSource(new StringReader(content));
//
//        try {
//            xsdSchema.newValidator().validate(xmlInSource);
//        } catch (SAXException | IOException e) {
//            throw new IntegrationException(Code.VALIDATION_FAILED, translate("error.invalidXmlContent", e));
//        }
    }
}
