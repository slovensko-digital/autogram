package digital.slovensko.autogram.core.eforms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.errors.InvalidXMLException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;

import org.w3c.dom.DOMException;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public abstract class EFormUtils {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static String extractTransformationOutputMimeTypeString(String transformation)
            throws TransformationParsingErrorException {
        if (transformation == null)
            return "TXT";

        var method = "";
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            var document = builderFactory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(transformation)));
            var elem = document.getDocumentElement();
            var outputElements = elem.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "output");
            if (outputElements.getLength() == 0)
                throw new TransformationParsingErrorException("Failed to parse transformation. Missing output element");

            method = outputElements.item(0).getAttributes().getNamedItem("method").getNodeValue();

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TransformationParsingErrorException("Failed to parse transformation");
        }

        if (method.equals("html"))
            return "HTML";

        if (method.equals("text"))
            return "TXT";

        return null;
    }

    public static String computeDigest(byte[] data, String canonicalizationMethod, DigestAlgorithm digestAlgorithm, Charset encoding) {
        var canonicalizedData = DSSXMLUtils.canonicalize(canonicalizationMethod, data);
        var digest = DSSUtils.digest(digestAlgorithm, canonicalizedData);
        var asBase64 = Base64.getEncoder().encode(digest);

        return new String(asBase64, encoding);
    }

    public static String getDigestValueFromElement(Element xdc, String elementLocalName) throws InvalidXMLException {
        var element = xdc.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", elementLocalName)
                .item(0);
        if (element == null)
            throw new InvalidXMLException("XML Datacontainer validation failed", "Element " + elementLocalName + " not found");

        var attributes = element.getAttributes();
        if (attributes == null || attributes.getLength() == 0)
            throw new InvalidXMLException("XML Datacontainer validation failed", "Attributes of " + elementLocalName + " not found");

        var digestValue = attributes.getNamedItem("DigestValue");
        if (digestValue == null)
            throw new InvalidXMLException("XML Datacontainer validation failed", "DigestValue of " + elementLocalName + " not found");

        return digestValue.getNodeValue();
    }

    public static String getFormUri(Element xdc) {
        var xmlData = xdc.getElementsByTagNameNS(
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData").item(0);

        if (xmlData == null)
            return null;

        return xmlData.getAttributes().getNamedItem("Identifier").getNodeValue();
    }

    public static String getNamespaceFromEformXml(Node xml) {
        var xmlns = xml.getAttributes().getNamedItem("xmlns");
        if (xmlns == null)
            return null;

        return xmlns.getNodeValue();
    }

    public static byte[] getTransformation(String url) {
        return getResource(url + "/form.sb.xslt");
    }

    public static byte[] getSchema(String url) {
        return getResource(url + "/form.xsd");
    }

    private static byte[] getResource(String url) {
        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(21600000);
        offlineFileLoader.setDataLoader(new CommonsDataLoader());

        DSSDocument xsltDoc;
        try {
            xsltDoc = offlineFileLoader.getDocument(url);
        } catch (DSSException e) {
            return null;
        }

        if (xsltDoc == null)
            return null;

        try {
            return xsltDoc.openStream().readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public static Document getXmlFromDocument(DSSDocument documentToDisplay) throws InvalidXMLException {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

            var is = documentToDisplay.openStream();
            var inputSource = new InputSource(is);
            inputSource.setEncoding(ENCODING.displayName());
            var parsedDocument = builderFactory.newDocumentBuilder().parse(inputSource);

            return parsedDocument;

        } catch (Exception e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Unable to parse xml content");
        }
    }

    public static Document getEformXmlFromXdcDocument(DSSDocument document) throws InvalidXMLException {
        var xmlDocument = getXmlFromDocument(document);
        var xmlData = xmlDocument.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData").item(0);

        if (xmlData == null)
            throw new InvalidXMLException("XML Datacontainer validation failed", "XMLData not found in XDC");

        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        Document responseDocument = null;
        try {
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            responseDocument = builderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Unable to create response document");
        }

        if (xmlData.getFirstChild() == null)
            throw new InvalidXMLException("XML Datacontainer validation failed", "XMLData is empty");

        var node = responseDocument.importNode(xmlData.getFirstChild(), true);
        responseDocument.appendChild(node);

        return responseDocument;
    }

    public static String transformElementToString(Node element) throws InvalidXMLException {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

            var document = builderFactory.newDocumentBuilder().newDocument();
            Node node;
            try {
                node = document.importNode(element, true);
            } catch (DOMException e) {
                node = document.importNode(element.getFirstChild(), true);
            }

            document.appendChild(node);

            var factory = TransformerFactory.newInstance();
            var transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            var writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Unable to get xml content");
        }
    }

    public static String transform(DSSDocument documentToDisplay, String transformation) throws TransformerException {
        try {
            var parsedDocument = getXmlFromDocument(documentToDisplay);
            var xmlSource = new DOMSource(parsedDocument);
            if (isXDC(documentToDisplay.getMimeType())) {
                xmlSource = new DOMSource(getEformXmlFromXdcDocument(documentToDisplay));
            }

            var transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            var transformer = transformerFactory.newTransformer(new StreamSource(
                new ByteArrayInputStream(transformation.getBytes(ENCODING))));

            var outputProperties = new Properties();
            outputProperties.setProperty(OutputKeys.ENCODING, ENCODING.displayName());
            transformer.setOutputProperties(outputProperties);
            var outputTarget = new StreamResult(new StringWriter());

            transformer.transform(xmlSource, outputTarget);

            return outputTarget.getWriter().toString();

        } catch (Exception e) {
            throw new TransformerException("Transformation failed", e);
        }
    }

    public static boolean validateXmlContentAgainstXsd(String xmlContent, String xsdSchema) {
        if (xsdSchema == null)
            return true;

        try {
            var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var schema = factory.newSchema(new StreamSource(new StringReader(xsdSchema)));
            var validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));

            return true;

        } catch (SAXException | IOException | IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isXDCContent(DSSDocument document) {
        Document doc;

        try {
            doc = getXmlFromDocument(document);
        } catch (InvalidXMLException e) {
            return false;
        }

        var docString = "";
        try {
            docString = transformElementToString(doc.getDocumentElement());

        } catch (InvalidXMLException e) {
            return false;
        }

        try {
        var xdcSchema = EFormUtils.class.getResourceAsStream("xmldatacontainer.xsd");
            return validateXmlContentAgainstXsd(docString, new String(xdcSchema.readAllBytes(), ENCODING));

        } catch (IOException | NullPointerException e) {
            return false;
        }
    }
}
