package com.octosign.whitelabel.communication;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Document to be signed
 */
public class Document implements Cloneable {
    private String id;
    private String name;
    private String title;
    private String content;
    private String transformation;

    public Document(
        String id,
        String name,
        String title,
        String content,
        String transformation
    ) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.content = content;
        this.transformation = transformation;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Document clone() {
        return new Document(id, name, title, content, transformation);
    }
}
