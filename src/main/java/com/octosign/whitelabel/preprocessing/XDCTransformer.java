package com.octosign.whitelabel.preprocessing;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.error_handling.*;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.octosign.whitelabel.ui.Utils.isPresent;


public class XDCTransformer {
    public enum Mode {
        IDEMPOTENT, RUSSIAN_DOLL
    }

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

    public static XDCTransformer newInstance(SignatureParameters sp) {
        var method = SignatureParameterMapper.map(sp.getPropertiesCanonicalization());
        var algorithm = SignatureParameterMapper.map(sp.getDigestAlgorithm());
        var mediaType = SignatureParameterMapper.map(sp.getTransformationOutputMimeType());

        return new XDCTransformer(sp.getIdentifier(), sp.getSchema(), sp.getTransformation(), sp.getContainerXmlns(), method, algorithm, mediaType);
    }

    private XDCTransformer(String identifier, String xsdSchema, String xsltSchema, String containerXmlns, String canonicalizationMethod, DigestAlgorithm digestAlgorithm, DestinationMediaType mediaDestinationTypeDescription) {
        int lastSlashIndex = identifier.lastIndexOf("/");
        if (lastSlashIndex == -1)
            throw new IntegrationException(Code.MALFORMED_INPUT, "Identifier contains no slash: " + identifier);

        this.identifierUri = identifier.substring(0, lastSlashIndex + 1);
        this.identifierVersion = identifier.substring(lastSlashIndex + 1);
        this.containerXmlns = containerXmlns;
        this.canonicalizationMethod = canonicalizationMethod;
        this.xsdSchema = xsdSchema;
        this.xsltSchema = xsltSchema;
        this.digestAlgorithm = digestAlgorithm;
        this.mediaDestinationTypeDescription = mediaDestinationTypeDescription;
    }

    public String transform(String xmlInput, Mode mode) {
        try {
            parseDOMDocument(xmlInput);
        } catch (SAXException | IOException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        try {
            transformDocument(mode);
        } catch (DOMException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        }

        try {
            return getDocumentContent();
        } catch (TransformerException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        }
    }

    public byte[] transform(byte[] xmlByteArrayInput, Mode mode) {
        try {
            parseDOMDocument(xmlByteArrayInput);
        } catch (SAXException | IOException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        try {
            transformDocument(mode);
        } catch (DOMException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        }

        try {
            return getDocumentContent().getBytes(StandardCharsets.UTF_8);
        } catch (TransformerException e) {
            throw new IntegrationException(Code.INVALID_CONTENT, e);
        }
    }

    private void parseDOMDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        var source = new InputSource(new StringReader(xmlContent));
        this.document = builderFactory.newDocumentBuilder().parse(source);
    }

    private void parseDOMDocument(byte[] xmlContent) throws ParserConfigurationException, IOException, SAXException {
        parseDOMDocument(new String(xmlContent, StandardCharsets.UTF_8));
    }

    private void transformDocument(Mode mode) {
        if (mode == Mode.IDEMPOTENT && isXDCAlreadyPresent())
            return;

        var root = document.getDocumentElement();

        var xmlDataContainer = createXMLDataContainer();
        var xmlData = createXMLData();
        var usedSchemasReferenced = createUsedSchemasReferenced();

        xmlDataContainer.appendChild(xmlData);
        xmlData.appendChild(root);
        if (usedSchemasReferenced != null) {
            xmlDataContainer.appendChild(usedSchemasReferenced);
        }

        document.appendChild(xmlDataContainer);
    }

    private boolean isXDCAlreadyPresent() {
        return document.getFirstChild().getNodeName().equalsIgnoreCase("XMLDataContainer");
    }

    private String getDocumentContent() throws TransformerException {
        document.setXmlStandalone(true);
        var xmlSource = new DOMSource(document);
        var outputTarget = new StreamResult(new StringWriter());

        var transformerFactory= TransformerFactory.newInstance();
        transformerFactory.newTransformer().transform(xmlSource, outputTarget);

        return outputTarget.getWriter().toString();
    }

    private Element createXMLDataContainer() {
        var element = document.createElement("XMLDataContainer");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        if (containerXmlns != null)
            element.setAttribute("xmlns", containerXmlns);

        return element;
    }

    private Element createXMLData() {
        var element = document.createElement("XMLData");
        element.setAttribute("ContentType", "application/xml; charset=UTF-8");
        element.setAttribute("Identifier", identifierUri);
        element.setAttribute("Version", identifierVersion);

        return element;
    }

    private Element createUsedSchemasReferenced() {
        var element = document.createElement("UsedSchemasReferenced");
        if (isPresent(xsdSchema) || isPresent(xsltSchema)) {
            var documentXmlnsNode = document.getFirstChild().getAttributes().getNamedItem("xmlns");
            if (documentXmlnsNode != null)
                documentXmlns = documentXmlnsNode.getNodeValue();
            else
                documentXmlns = "";
        }

        if (xsdSchema != null) {
            var xsdSchemaReference = createUsedXSDReference();
            element.appendChild(xsdSchemaReference);
        }
        if (xsltSchema != null) {
            var xsltSchemaReference = createUsedPresentationSchemaReference();
            element.appendChild(xsltSchemaReference);
        }

        return element;
    }

    private String computeDigest(String data) {
        byte[] asBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] canonicalizedData = DSSXMLUtils.canonicalize(canonicalizationMethod, asBytes);
        byte[] digest = DSSUtils.digest(digestAlgorithm, canonicalizedData);
        byte[] asBase64 = Base64.getEncoder().encode(digest);

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
        var element = document.createElement("UsedXSDReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue", computeDigest(xsdSchema));
        element.setTextContent(buildXSDReference());

        return element;
    }

    private Element createUsedPresentationSchemaReference() {
        var element = document.createElement("UsedPresentationSchemaReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue", computeDigest(xsltSchema));
        element.setAttribute("ContentType", "application/xslt+xml");
        element.setAttribute("MediaDestinationTypeDescription", mediaDestinationTypeDescription.name());
        element.setTextContent(buildXSLTReference());

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) {
        return "urn:oid:" + digestAlgorithm.getOid();
    }
}
