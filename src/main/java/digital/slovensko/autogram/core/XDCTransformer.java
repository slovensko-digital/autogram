package digital.slovensko.autogram.core;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XDCTransformer {

    public enum DestinationMediaType {
        TXT, HTML, XHTML
    }

    private final String identifierUri;
    private final String identifierVersion;
    private final String xsdSchema;
    private final String xsltSchema;
    private final String canonicalizationMethod;
    private final String containerXmlns;
    private final DigestAlgorithm digestAlgorithm;
    private final DestinationMediaType mediaDestinationTypeDescription;

    private Document document;
    private String documentXmlns;

    public static XDCTransformer buildFromSigningParameters(SigningParameters sp) {
        var mediaType = DestinationMediaType.TXT;

        var outputMime = sp.getTransformationOutputMimeType();
        if (outputMime != null && outputMime.equals(MimeTypeEnum.HTML))
            mediaType = DestinationMediaType.HTML;

        return new XDCTransformer(sp.getIdentifier(), sp.getSchema(), sp.getTransformation(), sp.getContainerXmlns(),
                sp.getPropertiesCanonicalization(), sp.getDigestAlgorithm(), mediaType);
    }

    private XDCTransformer(String identifier, String xsdSchema, String xsltSchema, String containerXmlns,
            String canonicalizationMethod, DigestAlgorithm digestAlgorithm,
            DestinationMediaType mediaDestinationTypeDescription) {
        int lastSlashIndex = identifier.lastIndexOf("/");
        if (lastSlashIndex == -1)
            throw new RuntimeException("Identifier contains no slash: " + identifier);

        this.identifierUri = identifier;
        this.identifierVersion = identifier.substring(lastSlashIndex + 1);
        this.containerXmlns = containerXmlns;
        this.canonicalizationMethod = canonicalizationMethod;
        this.xsdSchema = xsdSchema;
        this.xsltSchema = xsltSchema;
        this.digestAlgorithm = digestAlgorithm;
        this.mediaDestinationTypeDescription = mediaDestinationTypeDescription;
    }

    public static XDCTransformer buildFromSigningParametersAndDocument(SigningParameters sp, InMemoryDocument document) {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            return new XDCTransformer(sp.getSchema(),
                    sp.getTransformation(),
                    sp.getPropertiesCanonicalization(),
                    sp.getDigestAlgorithm(),
                    builderFactory.newDocumentBuilder().parse(new InputSource(document.openStream())));
        } catch (Exception e) {
            throw new RequestValidationException("XML Datacontainer validation failed", "Unable to process document");
        }
    }

    private XDCTransformer(String xsdSchema, String xsltSchema, String canonicalizationMethod,
                          DigestAlgorithm digestAlgorithm, Document document) {
        this.xsdSchema = xsdSchema;
        this.xsltSchema = xsltSchema;
        this.canonicalizationMethod = canonicalizationMethod;
        this.digestAlgorithm = digestAlgorithm;
        this.identifierUri = null;
        this.identifierVersion = null;
        this.containerXmlns = null;
        this.mediaDestinationTypeDescription = null;
        this.document = document;
    }

    public DSSDocument transform(DSSDocument dssDocument) {
        try {
            var xmlByteArrayInput = dssDocument.openStream().readAllBytes();
            parseDOMDocument(new String(xmlByteArrayInput, StandardCharsets.UTF_8));
            transformDocument();
            var content = getDocumentContent().getBytes(StandardCharsets.UTF_8);

            return new InMemoryDocument(content, dssDocument.getName());
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (DOMException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseDOMDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        var source = new InputSource(new StringReader(xmlContent));
        this.document = builderFactory.newDocumentBuilder().parse(source);
    }

    private void transformDocument() {
        var root = document.getDocumentElement();
        var xmlDataContainer = createXMLDataContainer();
        var xmlData = createXMLData();
        var usedSchemasReferenced = createUsedSchemasReferenced();

        xmlDataContainer.appendChild(xmlData);
        xmlData.appendChild(root);
        if (usedSchemasReferenced != null)
            xmlDataContainer.appendChild(usedSchemasReferenced);

        document.appendChild(xmlDataContainer);
    }

    private String getDocumentContent() throws TransformerException {
        document.setXmlStandalone(true);
        var xmlSource = new DOMSource(document);
        var outputTarget = new StreamResult(new StringWriter());
        var transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.newTransformer().transform(xmlSource, outputTarget);

        return outputTarget.getWriter().toString();
    }

    private Element createXMLDataContainer() {
        var element = document.createElement("xdc:XMLDataContainer");
        if (containerXmlns != null)
            element.setAttribute("xmlns:xdc", containerXmlns);

        return element;
    }

    private Element createXMLData() {
        var element = document.createElement("xdc:XMLData");
        element.setAttribute("ContentType", "application/xml; charset=UTF-8");
        element.setAttribute("Identifier", identifierUri);
        element.setAttribute("Version", identifierVersion);

        return element;
    }

    private static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    private Element createUsedSchemasReferenced() {
        var element = document.createElement("xdc:UsedSchemasReferenced");
        if ((!isNullOrBlank(xsdSchema)) || (!isNullOrBlank(xsltSchema))) {
            var documentXmlnsNode = document.getFirstChild().getAttributes().getNamedItem("xmlns");
            if (documentXmlnsNode != null)
                documentXmlns = documentXmlnsNode.getNodeValue();
            else
                documentXmlns = "";
        }

        if (xsdSchema != null)
            element.appendChild(createUsedXSDReference());

        if (xsltSchema != null)
            element.appendChild(createUsedPresentationSchemaReference());

        return element;
    }

    private String computeDigest(String data) {
        var asBytes = data.getBytes(StandardCharsets.UTF_8);
        var canonicalizedData = DSSXMLUtils.canonicalize(canonicalizationMethod, asBytes);
        var digest = DSSUtils.digest(digestAlgorithm, canonicalizedData);
        var asBase64 = Base64.getEncoder().encode(digest);

        return new String(asBase64, StandardCharsets.UTF_8);
    }

    // TODO: These should be configurable
    private String buildXSDReference() {
        return toURIString(documentXmlns, "form.xsd");
    }

    private String buildXSLTReference() {
        return toURIString(documentXmlns, "form.xslt");
    }

    private static String toURIString(String xmlns, String suffix) {
        String base = xmlns.replaceAll("/+$", "");
        String attached = suffix.replaceAll("^/+", "");

        return base + "/" + attached;
    }

    private Element createUsedXSDReference() {
        var element = document.createElement("xdc:UsedXSDReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue", computeDigest(xsdSchema));
        element.setTextContent(buildXSDReference());

        return element;
    }

    private Element createUsedPresentationSchemaReference() {
        var element = document.createElement("xdc:UsedPresentationSchemaReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue", computeDigest(xsltSchema));
        element.setAttribute("ContentType", "application/xslt+xml");
        element.setAttribute("MediaDestinationTypeDescription", mediaDestinationTypeDescription.name());
        element.setAttribute("Language", "sk");
        element.setTextContent(buildXSLTReference());

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) {
        return "urn:oid:" + digestAlgorithm.getOid();
    }

    public boolean validateXsdDigest() {
        String xsdSchemaHash = computeDigest(xsdSchema);
        String xsdDigestValue = getDigestValueFromElement("UsedXSDReference");
        return xsdSchemaHash.equals(xsdDigestValue);
    }

    public boolean validateXsltDigest() {
        String xsltSchemaHash = computeDigest(xsltSchema);
        String xsltDigestValue = getDigestValueFromElement("UsedPresentationSchemaReference");
        return xsltSchemaHash.equals(xsltDigestValue);
    }

    private String getDigestValueFromElement(String elementLocalName) {
        var xdc = document.getDocumentElement();

        var element = xdc.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", elementLocalName)
                .item(0);
        if (element == null)
            throw new RequestValidationException("XML Datacontainer validation failed", "Element " + elementLocalName + " not found");

        var attributes = element.getAttributes();
        if (attributes == null)
            throw new RequestValidationException("XML Datacontainer validation failed", "Attributes of " + elementLocalName + " not found");

        var digestValue = attributes.getNamedItem("DigestValue");
        if (digestValue == null)
            throw new RequestValidationException("XML Datacontainer validation failed", "DigestValue of " + elementLocalName + " not found");

        return digestValue.getNodeValue();
    }

    public String getContentFromXdc() {
        var xdc = document.getDocumentElement();

        var xmlData = xdc.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData")
                .item(0);

        if (xmlData == null)
            throw new RequestValidationException("XML Datacontainer validation failed", "XMLData not found in XDC");

        return transformElementToString(xmlData.getFirstChild());
    }

    private String transformElementToString(Node element) {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            var document = builderFactory.newDocumentBuilder().newDocument();
            var node = document.importNode(element, true);
            document.appendChild(node);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new RequestValidationException("XML Datacontainer validation failed", "Unable to get xml content");
        }
    }

}
