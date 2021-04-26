package com.octosign.whitelabel.communication.document;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XMLDocument extends Document {

    protected String transformation;

    public XMLDocument() { }

    public XMLDocument(
        String id,
        String title,
        String content,
        String legalEffect,
        String transformation
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.legalEffect = legalEffect;
        this.transformation = transformation;
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
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public String getTransformed() throws TransformerFactoryConfigurationError, TransformerException {
        if (content == null || transformation == null) {
            throw new RuntimeException("Document has no content or transformation defined");
        }

        var xslSource = new StreamSource(new StringReader(transformation));
        var xmlInSource = new StreamSource(new StringReader(content));

        var tf = TransformerFactory.newInstance().newTransformer(xslSource);

        var xmlOutWriter = new StringWriter();
        tf.transform(xmlInSource, new StreamResult(xmlOutWriter));

        return xmlOutWriter.toString();
    }

}
