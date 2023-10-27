package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.TransformationException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class XDCBuilder {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final String identifierUri;
    private final String identifierVersion;
    private final String xsdSchema;
    private final String xsltSchema;
    private final String canonicalizationMethod;
    private final String containerXmlns;
    private final DigestAlgorithm digestAlgorithm;
    private final String mediaDestinationTypeDescription;

    private Document document;
    private String documentXmlns;

    public static XDCBuilder buildFromSigningParameters(SigningParameters sp) {
        return new XDCBuilder(sp.getIdentifier(), sp.getSchema(), sp.getTransformation(), sp.getContainerXmlns(),
                sp.getPropertiesCanonicalization(), sp.getDigestAlgorithm(), sp.extractTransformationOutputMimeTypeString());
    }

    private XDCBuilder(String identifier, String xsdSchema, String xsltSchema, String containerXmlns,
            String canonicalizationMethod, DigestAlgorithm digestAlgorithm,
            String mediaDestinationTypeDescription) {
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

    public DSSDocument transform(DSSDocument dssDocument) {
        try {
            var xmlByteArrayInput = dssDocument.openStream().readAllBytes();
            parseDOMDocument(new String(xmlByteArrayInput, ENCODING));
            transformDocument();
            var content = getDocumentContent().getBytes(ENCODING);

            return new InMemoryDocument(content, dssDocument.getName());

        } catch (TransformationException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformationException("Nastala chyba počas transformácie dokumentu", "Nastala chyba počas transformácie dokumentu", e);
        }
    }

    private void parseDOMDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

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

    private String getDocumentContent() throws TransformationException, TransformerException {
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
        element.setAttribute("DigestValue", computeDigest(xsdSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING));
        element.setTextContent(buildXSDReference());

        return element;
    }

    private Element createUsedPresentationSchemaReference() {
        var element = document.createElement("xdc:UsedPresentationSchemaReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue", computeDigest(xsltSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING));
        element.setAttribute("ContentType", "application/xslt+xml");
        element.setAttribute("MediaDestinationTypeDescription", mediaDestinationTypeDescription);
        element.setAttribute("Language", "sk");
        element.setTextContent(buildXSLTReference());

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) {
        return "urn:oid:" + digestAlgorithm.getOid();
    }
}
