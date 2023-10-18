package digital.slovensko.autogram.core.visualization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Base64;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.AutogramMimeType;
import static digital.slovensko.autogram.core.AutogramMimeType.*;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
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
        return createVisualization(job);
    }

    private Visualization createVisualization(SigningJob job)
        throws IOException, ParserConfigurationException, SAXException, TransformerException {

        var documentToDisplay = document;
        if (isAsice(documentToDisplay.getMimeType())) {
            try {
                documentToDisplay = AsicContainerUtils.getOriginalDocument(document);
            } catch (AutogramException e) {
                return new UnsupportedVisualization(job);
            }
        }

        var transformation = getTransformation(documentToDisplay);
        var transformationOutputMimeType = parameters.extractTransformationOutputMimeTypeString();

        if (isDocumentSupportingTransformation(documentToDisplay) && isTranformationAvailable(transformation)) {
            if (transformationOutputMimeType.equals("HTML"))
                return new HTMLVisualization(transform(documentToDisplay, transformation), job);

            if (transformationOutputMimeType.equals("TEXT"))
                return new PlainTextVisualization(transform(documentToDisplay, transformation), job);

            return new UnsupportedVisualization(job);
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(transform(documentToDisplay, transformation), job);

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.TEXT))
            return new PlainTextVisualization(new String(documentToDisplay.openStream().readAllBytes()), job);

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.PDF))
            return new PDFVisualization(documentToDisplay, job);

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.JPEG) || documentToDisplay.getMimeType().equals(MimeTypeEnum.PNG))
            return new ImageVisualization(documentToDisplay, job);

        return new UnsupportedVisualization(job);
    }

    private String getTransformation(DSSDocument documentToDisplay) {
        if (parameters.getTransformation() != null)
            return parameters.getTransformation();

        if (!documentToDisplay.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER))
            return null;

        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        try {
            var is = documentToDisplay.openStream();
            var inputSource = new InputSource(is);
            inputSource.setEncoding(encoding.displayName());
            var parsedDocument = builderFactory.newDocumentBuilder().parse(inputSource);
            var xdc = parsedDocument.getDocumentElement();
            var xmlData = xdc.getElementsByTagNameNS(
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData").item(0);

            if (xmlData == null)
                return null;

            var uri = xmlData.getAttributes().getNamedItem("Identifier").getNodeValue();
            var xdcXsltDigest = xdc.getElementsByTagNameNS(
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "UsedXSDReference").item(0).getAttributes().getNamedItem("DigestValue").getNodeValue();
            return findTransformation(uri, xdcXsltDigest);

        } catch (Exception e) {
            return null;
        }
    }

    private String findTransformation(String uri, String xdcXsltDigest) {
        var parts = uri.split("/");
        var lastPart = parts[parts.length - 1];
        var secondLastPart = parts[parts.length - 2];
        var directory = secondLastPart + "/" + lastPart;
        var metaFile = DocumentVisualizationBuilder.class.getResourceAsStream(directory + "/META-INF/manifest.xml");

        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        try {
            var inputSource = new InputSource(metaFile);
            inputSource.setEncoding(encoding.displayName());
            var parsedDocument = builderFactory.newDocumentBuilder().parse(inputSource);
            var xml = parsedDocument.getDocumentElement();

            String xslt = null;
            var entries = xml.getElementsByTagName("manifest:file-entry");

            for (int i = 0; i < entries.getLength(); i++) {
                var entry = entries.item(i);
                var mediaDestinationNode = entry.getAttributes().getNamedItem("media-destination");
                if (mediaDestinationNode == null)
                    continue;

                var mediaDestination = mediaDestinationNode.getNodeValue();
                var fullPath = entry.getAttributes().getNamedItem("full-path");
                if (!mediaDestination.equals("sign") || fullPath == null)
                    continue;

                xslt = fullPath.getNodeValue().replace("\\", "/");
                var transformation = DocumentVisualizationBuilder.class.getResourceAsStream(directory + "/" + xslt).readAllBytes();

                var canonicalizedData = DSSXMLUtils.canonicalize(CanonicalizationMethod.INCLUSIVE, transformation);
                var digest = DSSUtils.digest(DigestAlgorithm.SHA256, canonicalizedData);
                var asBase64 = Base64.getEncoder().encode(digest);

                var xsltDigest = new String(asBase64, StandardCharsets.UTF_8);

                if (xsltDigest.equals(xdcXsltDigest))
                    ;
                else
                    System.out.println("XSLT digest mismatch: " + xsltDigest + " != " + xdcXsltDigest);


                System.out.println("Content: " + new String(transformation, encoding));
                return new String(transformation, encoding);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    private boolean isDocumentSupportingTransformation(DSSDocument document) {
        return document.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER)
            || document.getMimeType().equals(AutogramMimeType.APPLICATION_XML)
            || document.getMimeType().equals(MimeTypeEnum.XML);
    }

    private boolean isDocumentXDC(DSSDocument documentToDisplay) {
        return documentToDisplay.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER);
    }

    /**
     * Transform document (XML) using transformation (XSLT)
     * @param transformation
     *
     * @return transformed document string
     */
    private String transform(DSSDocument documentToDisplay, String transformation)
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
            var transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            var transformer = transformerFactory.newTransformer(new StreamSource(
                new ByteArrayInputStream(transformation.getBytes(encoding))));

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
