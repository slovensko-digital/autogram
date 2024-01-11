package digital.slovensko.autogram.core.eforms;

import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;

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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.util.XMLUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;

public abstract class EFormUtils {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    public static final String XDC_XMLNS = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";

    public static String extractTransformationOutputMimeTypeString(String transformation)
            throws TransformationParsingErrorException {
        if (transformation == null)
            return "TXT";

        // remove BOM if present
        if (transformation.length() > 0 && transformation.charAt(0) == '\uFEFF')
            transformation = transformation.substring(1);

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

            if (method.equals("html"))
                return "HTML";

            if (method.equals("text"))
                return "TXT";

            if (method.equals("xml")) {
                var doctypeSystemAttribute = outputElements.item(0).getAttributes().getNamedItem("doctype-system");
                if (doctypeSystemAttribute != null && doctypeSystemAttribute.getNodeValue().equals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"))
                    return "XHTML";
            }

            throw new TransformationParsingErrorException("Failed to parse transformation. Unsupported output method: " + method);

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TransformationParsingErrorException("Failed to parse transformation output method");
        }
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
        var element = xdc.getElementsByTagNameNS(XDC_XMLNS, elementLocalName).item(0);
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

    public static Element getElementFromXdc(Element xdc, String elementLocalName) {
        var element = xdc.getElementsByTagNameNS(XDC_XMLNS, elementLocalName).item(0);
        if (element == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element " + elementLocalName + " nebol nájdený");

        return (Element) element;
    }

    public static String getValueFromElement(Element xdc, String elementLocalName) {
        return getElementFromXdc(xdc, elementLocalName).getTextContent();
    }

    public static String transformElementToString(Element element) {
        try {
            var document = XMLUtils.getSecureDocumentBuilder().newDocument();
            Node node;
            try {
                node = document.importNode(element, true);
            } catch (DOMException e) {
                node = document.importNode(element.getFirstChild(), true);
            }

            document.appendChild(node);

            var transformer = XMLUtils.getSecureTransformerFactory().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            var writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dokument", e);
        }
    }

    public static XsltParams getXsltParamsFromXsltReference(Element xdc) {
        var element = xdc.getElementsByTagNameNS(XDC_XMLNS, "UsedPresentationSchemaReference").item(0);
        if (element == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Element UsedPresentationSchemaReference nebol nájdený");

        var identifier = element.getTextContent();

        var attributes = element.getAttributes();
        if (attributes == null || attributes.getLength() == 0)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Atribúty v UsedPresentationSchemaReference neboli nájdené");

        var languageNode = attributes.getNamedItem("Language");
        var language = languageNode == null ? null : languageNode.getNodeValue();

        var mediaDestiantionTypeNode = attributes.getNamedItem("MediaDestinationTypeDescription");
        var mediaDestiantionType = mediaDestiantionTypeNode == null ? null : mediaDestiantionTypeNode.getNodeValue();

        var targetNode = attributes.getNamedItem("TargetEnvironment");
        var target = targetNode == null ? null : targetNode.getNodeValue();

        var mediaTypeNode = attributes.getNamedItem("MediaType");
        var mediaType = mediaTypeNode == null ? null : mediaTypeNode.getNodeValue();

        return new XsltParams(identifier, language, mediaDestiantionType, target, mediaType);
    }

    public static String getFormUri(Element xdc) {
        var xmlData = xdc.getElementsByTagNameNS(
                XDC_XMLNS, "XMLData").item(0);

        if (xmlData == null)
            return null;

        var identifierNode = xmlData.getAttributes().getNamedItem("Identifier");

        if (identifierNode != null)
            return identifierNode.getNodeValue();

        var firstChild = xmlData.getFirstChild();
        if (firstChild == null)
            return null;

        var xsiSchemaLocationNode = firstChild.getAttributes().getNamedItem("xsi:schemaLocation");
        if (xsiSchemaLocationNode == null)
            return null;

        return xsiSchemaLocationNode.getNodeValue();
    }

    public static String getNamespaceFromEformXml(Node xml) {
        var xmlns = xml.getAttributes().getNamedItem("xmlns");
        if (xmlns != null)
            return xmlns.getNodeValue();

        xmlns = xml.getAttributes().getNamedItem("xsi:schemaLocation");
        if (xmlns != null)
            return xmlns.getNodeValue();

        return null;
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
        var xmlData = xmlDocument.getElementsByTagNameNS(XDC_XMLNS, "XMLData").item(0);

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

            transformation = transformation.replaceAll("\r\n", "\n");
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

    public static XsltParams fillXsltParams(String transformation, String formIdentifier, XsltParams xsltParams) {
        var identifier = xsltParams.identifier();
        if (identifier == null && formIdentifier != null) {
            var t = formIdentifier.split("/");
            var url = t[t.length - 2] + "/" + t[t.length - 1];
            identifier = "http://schemas.gov.sk/form/" + url + "/form.xslt";
        }

        var language = xsltParams.language();
        if (language == null)
            language = "sk";

        var mediaType = xsltParams.mediaType();
        if (mediaType == null)
            mediaType = "application/xslt+xml";

        var destinationType = xsltParams.destinationType();
        if (destinationType == null)
            destinationType = extractTransformationOutputMimeTypeString(transformation);

        return new XsltParams(identifier, language, destinationType, xsltParams.target(), mediaType);
    }

    public static String fillXsdIdentifier(String formIdentifier) {
        if (formIdentifier == null)
            throw new XMLValidationException("Zlyhala príprava XML Datacontainera", "Nepodarilo sa vyrobiť identifikátor XSD schémy z identifikátora formulára");

        var t = formIdentifier.split("/");
        var url = t[t.length - 2] + "/" + t[t.length - 1];

        return "http://schemas.gov.sk/form/" + url + "/form.xsd";
    }
}
