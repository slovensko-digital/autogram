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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.core.errors.MultipleOriginalDocumentsFoundException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.util.AsicContainerUtils;
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

    public static String computeDigest(byte[] data, String canonicalizationMethod, DigestAlgorithm digestAlgorithm, Charset encoding) throws XMLValidationException {
        try {
            var canonicalizedData = DSSXMLUtils.canonicalize(canonicalizationMethod, data);
            var digest = DSSUtils.digest(digestAlgorithm, canonicalizedData);
            var asBase64 = Base64.getEncoder().encode(digest);

            return new String(asBase64, encoding);
        } catch (DSSException e) {
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa vypočítať odtlačok", e);
        }
    }

    public static String getDigestValueFromElement(Element xdc, String elementLocalName) throws XMLValidationException {
        var element = xdc.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", elementLocalName)
                .item(0);
        if (element == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element " + elementLocalName + " nebol nájdený");

        var attributes = element.getAttributes();
        if (attributes == null || attributes.getLength() == 0)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Atribúty v " + elementLocalName + " neboli nájdené");

        var digestValue = attributes.getNamedItem("DigestValue");
        if (digestValue == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Hodnota odtlačku v " + elementLocalName + " nebola nájdená");

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

    public static Document getXmlFromDocument(DSSDocument documentToDisplay) throws XMLValidationException {
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
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dokument", e);
        }
    }

    public static Document getEformXmlFromXdcDocument(DSSDocument document) throws XMLValidationException {
        var xmlDocument = getXmlFromDocument(document);
        var xmlData = xmlDocument.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData").item(0);

        if (xmlData == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element XMLData sa nepodarilo nájsť v XML Dataconatineri");

        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        Document responseDocument = null;
        try {
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            responseDocument = builderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new UnrecognizedException(e);
        }

        if (xmlData.getFirstChild() == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element XMLData je prázdny");

        var idk = xmlData.getFirstChild();
        if (idk.getNodeType() == Node.TEXT_NODE) {
            if (!idk.getNodeValue().matches("\\s*"))
                throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "XMLData obsahuje neplatný text");

            idk = idk.getNextSibling();
        }

        if (idk == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element XMLData je prázdny");

        try {
            var node = responseDocument.importNode(idk, true);
            responseDocument.appendChild(node);
        } catch (DOMException e) {
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dokument", e);
        }

        return responseDocument;
    }

    public static String transformElementToString(Node element) throws XMLValidationException {
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
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dokument", e);
        }
    }

    public static String transform(DSSDocument documentToDisplay, String transformation) throws TransformationException {
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
            throw new TransformationException("Zlyhala transformácia podľa XSLT", "Nepodarilo sa transformovať XML dokument podľa XSLT transformácie", e);
        }
    }

    public static DSSDocument getXmlDocument(DSSDocument document)
            throws XMLValidationException, OriginalDocumentNotFoundException,
            MultipleOriginalDocumentsFoundException {
        if (isAsice(document.getMimeType())) {
            var originalDocument = AsicContainerUtils.getOriginalDocument(document);
            if (!isXDC(originalDocument.getMimeType()))
                return null;

            return originalDocument;
        }

        return document;
    }
}
