package digital.slovensko.autogram.core.visualization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.europa.esig.dss.model.DSSDocument;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.AutogramMimeType;
import static digital.slovensko.autogram.core.AutogramMimeType.*;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.CommonDocument;

public class DocumentVisualizationBuilder {

    private final DSSDocument document;
    private final SigningParameters parameters;
    private final Charset encoding = StandardCharsets.UTF_8;

    private DocumentVisualizationBuilder(CommonDocument document, SigningParameters parameters) {
        this.document = document;
        this.parameters = parameters;
    }

    public static Visualization fromJob(SigningJob job) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        return new DocumentVisualizationBuilder(job.getDocument(), job.getParameters()).build(job);
    }

    private Visualization build(SigningJob job) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        var transformationOutputMime = getTransformationOutputMimeType(getTransformation());
        return createVisualization(job, transformationOutputMime);
    }

    private Visualization createVisualization(SigningJob job, MimeType transformationOutputMimeType)
        throws IOException, ParserConfigurationException, SAXException, TransformerException {

        var documentToDisplay = this.document;
        if (isAsice(documentToDisplay.getMimeType()))
            documentToDisplay = AsicContainerUtils.getOriginalDocument(this.document);

        if (isTranformationAvailable(getTransformation()) && isDocumentSupportingTransformation(documentToDisplay)) {

                // Applying transformation
                if (transformationOutputMimeType.equals(MimeTypeEnum.HTML)) {
                    return new HTMLVisualization(transform(documentToDisplay), job);
                } else if (transformationOutputMimeType.equals(MimeTypeEnum.TEXT)) {
                    return new PlainTextVisualization(transform(documentToDisplay), job);
                } else {
                    return new UnsupportedVisualization(job);
                }
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML)) {
            return new HTMLVisualization(transform(documentToDisplay), job);
        } else if (documentToDisplay.getMimeType().equals(MimeTypeEnum.TEXT)) {
            return new PlainTextVisualization(new String(documentToDisplay.openStream().readAllBytes()), job);
        } else if (documentToDisplay.getMimeType().equals(MimeTypeEnum.PDF)) {
            return new PDFVisualization(documentToDisplay, job);
        } else if (documentToDisplay.getMimeType().equals(MimeTypeEnum.JPEG)
            || documentToDisplay.getMimeType().equals(MimeTypeEnum.PNG)) {
            return new ImageVisualization(documentToDisplay, job);
        }

        return new UnsupportedVisualization(job);
    }



    private String getTransformation() {
        return parameters.getTransformation();
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    /**
     * Is document type which we can transform?
     *
     * @return
     */
    private boolean isDocumentSupportingTransformation(DSSDocument document) {
        return document.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER)
            || document.getMimeType().equals(AutogramMimeType.APPLICATION_XML)
            || document.getMimeType().equals(MimeTypeEnum.XML);
    }

    /**
     * Is document xml data container?
     */
    private boolean isDocumentXDC(DSSDocument documentToDisplay) {
        return documentToDisplay.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER);
    }

    public static MimeType getTransformationOutputMimeType(String transformation)
        throws SAXException, IOException, ParserConfigurationException {
        if (transformation == null)
            return null;

        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        var document = builderFactory.newDocumentBuilder()
            .parse(new InputSource(new StringReader(transformation)));
        var elem = document.getDocumentElement();
        var outputElements = elem.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "output");
        var method = outputElements.item(0).getAttributes().getNamedItem("method").getNodeValue();

        if (method.equals("html"))
            return MimeTypeEnum.HTML;

        if (method.equals("text"))
            return MimeTypeEnum.TEXT;

        throw new RuntimeException("Unsupported transformation output method: " + method);
    }

    /**
     * Transform document (XML) using transformation (XSLT)
     *
     * @return transformed document string
     */
    private String transform(DSSDocument documentToDisplay)
        throws IOException, ParserConfigurationException, SAXException, TransformerException {
        // We are using try catch instead of try-with-resources because
        // when debugging with VSCode on M2 MacOS, it throws self-suppression error
        // (which is weird)
        final var is = documentToDisplay.openStream();
        Throwable originalException = null;
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            var inputSource = new InputSource(is);
            inputSource.setEncoding(encoding.displayName());
            var parsedDocument = builderFactory.newDocumentBuilder().parse(inputSource);

            var xmlSource = new DOMSource(parsedDocument);
            if (isDocumentXDC(documentToDisplay))
                xmlSource = extractFromXDC(parsedDocument, builderFactory);

            var outputTarget = new StreamResult(new StringWriter());

            // var transformerFactory = TransformerFactory.newDefaultInstance();

            var transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);

            var transformer = transformerFactory.newTransformer(new StreamSource(
                new ByteArrayInputStream(getTransformation().getBytes(encoding))));
            var outputProperties = new Properties();
            outputProperties.setProperty(OutputKeys.ENCODING, encoding.displayName());
            transformer.setOutputProperties(outputProperties);
            transformer.transform(xmlSource, outputTarget);

            return outputTarget.getWriter().toString().trim();
        } catch (Exception transformationException) {
            originalException = transformationException;
            throw transformationException;
        } finally {
            if (is != null) {
                if (originalException != null) {
                    try {
                        is.close();
                    } catch (Throwable closeException) {
                        originalException.addSuppressed(closeException);
                    }

                } else {
                    is.close();
                }
            }
        }
    }

    private DOMSource extractFromXDC(Document document, DocumentBuilderFactory builderFactory)
        throws ParserConfigurationException {
        var xdc = document.getDocumentElement();

        var xmlData = xdc.getElementsByTagNameNS(
            "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData").item(0);

        if (xmlData == null)
            throw new RuntimeException("XMLData not found in XDC"); // TODO catch somewhere

        document = builderFactory.newDocumentBuilder().newDocument();
        var node = document.importNode(xmlData.getFirstChild(), true);
        document.appendChild(node);

        return new DOMSource(document);
    }

}
