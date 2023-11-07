package digital.slovensko.autogram.core.eforms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.util.XMLUtils;
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
            var document = XMLUtils.getSecureDocumentBuilder()
                    .parse(new InputSource(new StringReader(transformation)));
            var elem = document.getDocumentElement();
            var outputElements = elem.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "output");
            if (outputElements.getLength() == 0)
                throw new TransformationParsingErrorException("Failed to parse transformation. Missing output element");

            var methodAttribute = outputElements.item(0).getAttributes().getNamedItem("method");
            if (methodAttribute == null)
                throw new TransformationParsingErrorException("Failed to parse transformation. Missing output method attrbiute");

            method = methodAttribute.getNodeValue();

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TransformationParsingErrorException("Failed to parse transformation output method");
        }

        if (method.equals("html"))
            return "HTML";

        if (method.equals("text"))
            return "TXT";

        throw new TransformationParsingErrorException("Failed to parse transformation. Unsupported output method: " + method);
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

    public static byte[] getResource(String url) {
        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(21600000);  // 6 hours
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
            var is = documentToDisplay.openStream();
            var inputSource = new InputSource(is);
            inputSource.setEncoding(ENCODING.displayName());
            var parsedDocument = XMLUtils.getSecureDocumentBuilder().parse(inputSource);

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

        Document responseDocument = null;
        try {
            responseDocument = XMLUtils.getSecureDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new UnrecognizedException(e);
        }

        if (xmlData.getFirstChild() == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element XMLData je prázdny");

        var idk = xmlData.getFirstChild();
        // In an indented XML, whitespaces between XMLData and its content are considered as text nodes.
        // If there is a text node, validate it against all-whitespace regex and skip it.
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

    public static String transform(DSSDocument documentToDisplay, String transformation) throws TransformationException {
        try {
            var parsedDocument = getXmlFromDocument(documentToDisplay);
            var xmlSource = new DOMSource(parsedDocument);
            if (isXDC(documentToDisplay.getMimeType())) {
                xmlSource = new DOMSource(getEformXmlFromXdcDocument(documentToDisplay));
            }

            var transformer = XMLUtils.getSecureTransformerFactory().newTransformer(new StreamSource(
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
}
