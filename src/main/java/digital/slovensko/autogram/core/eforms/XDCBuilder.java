package digital.slovensko.autogram.core.eforms;

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

public abstract class XDCBuilder {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static DSSDocument transform(String identifier, String xsdSchema, String xsltSchema, String containerXmlns,
            String canonicalizationMethod, DigestAlgorithm digestAlgorithm,
            String mediaDestinationTypeDescription, DSSDocument dssDocument) {
        var lastSlashIndex = identifier.lastIndexOf("/");
        if (lastSlashIndex == -1)
            throw new RuntimeException("Identifier contains no slash: " + identifier);

        var identifierVersion = identifier.substring(lastSlashIndex + 1);
        try {
            var xmlByteArrayInput = dssDocument.openStream().readAllBytes();
            var parsedDocument = parseDOMDocument(new String(xmlByteArrayInput, ENCODING));
            var transformedDocument = transformDocument(parsedDocument, containerXmlns, identifier, identifierVersion,
                    xsdSchema, xsltSchema, canonicalizationMethod, digestAlgorithm, mediaDestinationTypeDescription);
            var content = getDocumentContent(transformedDocument).getBytes(ENCODING);

            return new InMemoryDocument(content, dssDocument.getName());

        } catch (TransformationException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformationException("Nastala chyba počas transformácie dokumentu",
                    "Nastala chyba počas transformácie dokumentu", e);
        }
    }

    private static Document parseDOMDocument(String xmlContent)
            throws ParserConfigurationException, IOException, SAXException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

        var source = new InputSource(new StringReader(xmlContent));
        return builderFactory.newDocumentBuilder().parse(source);
    }

    private static Document transformDocument(Document document, String containerXmlns, String identifierUri,
            String identifierVersion, String xsdSchema, String xsltSchema, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm, String mediaDestinationTypeDescription) {
        var root = document.getDocumentElement();
        var xmlDataContainer = createXMLDataContainer(document, containerXmlns);
        var xmlData = createXMLData(document, identifierUri, identifierVersion);
        var usedSchemasReferenced = createUsedSchemasReferenced(document, xsdSchema, xsltSchema, canonicalizationMethod,
                digestAlgorithm, mediaDestinationTypeDescription);

        xmlDataContainer.appendChild(xmlData);
        xmlData.appendChild(root);
        if (usedSchemasReferenced != null)
            xmlDataContainer.appendChild(usedSchemasReferenced);

        document.appendChild(xmlDataContainer);
        return document;
    }

    private static String getDocumentContent(Document document) throws TransformationException, TransformerException {
        document.setXmlStandalone(true);
        var xmlSource = new DOMSource(document);
        var outputTarget = new StreamResult(new StringWriter());
        var transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.newTransformer().transform(xmlSource, outputTarget);

        return outputTarget.getWriter().toString();
    }

    private static Element createXMLDataContainer(Document document, String containerXmlns) {
        var element = document.createElement("xdc:XMLDataContainer");
        if (containerXmlns != null)
            element.setAttribute("xmlns:xdc", containerXmlns);

        return element;
    }

    private static Element createXMLData(Document document, String identifierUri, String identifierVersion) {
        var element = document.createElement("xdc:XMLData");
        element.setAttribute("ContentType", "application/xml; charset=UTF-8");
        element.setAttribute("Identifier", identifierUri);
        element.setAttribute("Version", identifierVersion);

        return element;
    }

    private static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Element createUsedSchemasReferenced(Document document, String xsdSchema, String xsltSchema,
            String cannonicalizationMethod, DigestAlgorithm digestAlgorithm, String mediaDestinationTypeDescription) {
        var element = document.createElement("xdc:UsedSchemasReferenced");
        var documentXmlns = "";
        if ((!isNullOrBlank(xsdSchema)) || (!isNullOrBlank(xsltSchema))) {
            var documentXmlnsNode = document.getFirstChild().getAttributes().getNamedItem("xmlns");
            if (documentXmlnsNode != null)
                documentXmlns = documentXmlnsNode.getNodeValue();
        }

        if (xsdSchema != null)
            element.appendChild(createUsedXSDReference(document, cannonicalizationMethod, digestAlgorithm, xsdSchema,
                    documentXmlns));

        if (xsltSchema != null)
            element.appendChild(createUsedPresentationSchemaReference(document, cannonicalizationMethod,
                    digestAlgorithm, xsltSchema, mediaDestinationTypeDescription, documentXmlns));

        return element;
    }

    // TODO: These should be configurable
    private static String buildXSDReference(String documentXmlns) {
        return toURIString(documentXmlns, "form.xsd");
    }

    private static String buildXSLTReference(String documentXmlns) {
        return toURIString(documentXmlns, "form.xslt");
    }

    private static String toURIString(String xmlns, String suffix) {
        String base = xmlns.replaceAll("/+$", "");
        String attached = suffix.replaceAll("^/+", "");

        return base + "/" + attached;
    }

    private static Element createUsedXSDReference(Document document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm, String xsdSchema, String documentXmlns) {
        var element = document.createElement("xdc:UsedXSDReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue",
                computeDigest(xsdSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING));
        element.setTextContent(buildXSDReference(documentXmlns));

        return element;
    }

    private static Element createUsedPresentationSchemaReference(Document document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm, String xsltSchema, String mediaDestinationTypeDescription,
            String documentXmlns) {
        var element = document.createElement("xdc:UsedPresentationSchemaReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue",
                computeDigest(xsltSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING));
        element.setAttribute("ContentType", "application/xslt+xml");
        element.setAttribute("MediaDestinationTypeDescription", mediaDestinationTypeDescription);
        element.setAttribute("Language", "sk");
        element.setTextContent(buildXSLTReference(documentXmlns));

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) {
        return "urn:oid:" + digestAlgorithm.getOid();
    }
}
