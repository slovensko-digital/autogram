package com.octosign.whitelabel.communication.document;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * XML document for signing
 */
public class XmlDocument extends Document {

    public static final String MIME_TYPE = "application/xml";

    protected String transformation;

    public XmlDocument() { }

    public XmlDocument(Document document) {
        setId(document.getId());
        setTitle(document.getTitle());
        setContent(document.getContent());
        setLegalEffect(document.getLegalEffect());
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        transformation = transformation;
    }

    /**
     * Apply defined transformation on the document and get it
     *
     * @return String with the transformed XML document - for example its HTML representation
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public String getTransformed() throws TransformerFactoryConfigurationError, TransformerException {
        if (content == null || transformation == null) {
            throw new RuntimeException("Document has no content or transformation defined");
        }

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(content));

        var transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        var transformer = transformerFactory.newTransformer(xslSource);

        var xmlOutWriter = new StringWriter();
        transformer.transform(xmlInSource, new StreamResult(xmlOutWriter));

        return xmlOutWriter.toString();
    }

}
