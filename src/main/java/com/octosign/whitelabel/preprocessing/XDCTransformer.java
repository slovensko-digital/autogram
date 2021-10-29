package com.octosign.whitelabel.preprocessing;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.octosign.whitelabel.ui.Main.translate;
import static java.util.Objects.requireNonNull;


public class XDCTransformer {
    public enum Mode {
        IDEMPOTENT,
        RUSSIAN_DOLL
    }

    private final String identifier;
    private final String version;
    private final String xsdSchema;
    private final String xsltSchema;
    private final String canonicalizationMethod;
    private final DigestAlgorithm digestAlgorithm;

    private Document document;

    public static XDCTransformer newInstance(SignatureParameters sp) {
        var method = SignatureParameterMapper.map(sp.getPropertiesCanonicalization());
        var algorithm = SignatureParameterMapper.map(sp.getDigestAlgorithm());

        return new XDCTransformer(sp.getIdentifier(), sp.getVersion(), sp.getSchema(), sp.getTransformation(), method, algorithm);
    }

    private XDCTransformer(String identifier, String version, String xsdSchema, String xsltSchema, String canonicalizationMethod, DigestAlgorithm digestAlgorithm) {
        this.identifier = requireNonNull(identifier, "identifier");
        this.version = requireNonNull(version, "version");
        this.xsdSchema = requireNonNull(xsdSchema, "xsdSchema");
        this.xsltSchema = requireNonNull(xsltSchema, "xsltSchema");
        this.canonicalizationMethod = requireNonNull(canonicalizationMethod, "canonicalizationMethod");
        this.digestAlgorithm = requireNonNull(digestAlgorithm, "digestAlgorithm");
    }

    public String transform(String xmlInput, Mode mode) throws IntegrationException {
        try {
            parseDOMDocument(xmlInput);
        } catch (SAXException | IOException e) {
            throw new IntegrationException(Code.MALFORMED_INPUT, translate("error.xmlParsingFailure", e));
        } catch (ParserConfigurationException e) {
            throw new IntegrationException(Code.UNEXPECTED_ERROR, translate("error.builderConfigInvalid", e));
        }

        try {
            transformDocument(mode);
        } catch (DOMException e) {
            throw new IntegrationException(String.format("XML DOM related error: %s", e));
        }

        try {
            return getDocumentContent();
        } catch (TransformerException e) {
            throw new IntegrationException(String.format("Pre-final transformation failed: %s", e));
        }
    }

    private void parseDOMDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        var source = new InputSource(new StringReader(xmlContent));
        this.document = builderFactory.newDocumentBuilder().parse(source);
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
        xmlDataContainer.appendChild(usedSchemasReferenced);

        document.appendChild(xmlDataContainer);
    }

    private boolean isXDCAlreadyPresent() {
        return document.getFirstChild().getNodeName().equalsIgnoreCase("XMLDataContainer");
    }

    private String getDocumentContent() throws TransformerException {
        var xmlSource = new DOMSource(document);
        var outputTarget = new StreamResult(new StringWriter());

        var transformerFactory= TransformerFactory.newInstance();
        transformerFactory.newTransformer().transform(xmlSource, outputTarget);

        return outputTarget.toString();
    }

    private Element createXMLDataContainer() {
        var element = document.createElement("XMLDataContainer");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        // TODO: Make additional attributes configurable via parameters - this can't be hardcoded to data.gov.sk
        element.setAttribute("xmlns", "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1");

        return element;
    }

    private Element createXMLData() {
        var element = document.createElement("XMLData");
        element.setAttribute("ContentType", "application/xml; charset=UTF-8");
        element.setAttribute("Identifier", identifier);
        element.setAttribute("Version", version);

        return element;
    }

    private Element createUsedSchemasReferenced() {
        var element = document.createElement("UsedSchemasReferenced");
        var xsdSchemaReference = createUsedXSDReference();
        var xsltSchemaReference = createUsedPresentationSchemaReference();

        element.appendChild(xsdSchemaReference);
        element.appendChild(xsltSchemaReference);

        return element;
    }

    private String computeDigest(String data) {
        byte[] asBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] canonicalizedData = DSSXMLUtils.canonicalize(canonicalizationMethod, asBytes);
        byte[] digest = DSSUtils.digest(digestAlgorithm, canonicalizedData);
        byte[] asBase64 = Base64.getEncoder().encode(digest);

        return new String(asBase64, StandardCharsets.UTF_8);
    }

    private String buildXSDReference() { return toURIString(identifier, version,"form.xsd"); }
    private String buildXSLTReference() { return toURIString(identifier, version,"form.xslt"); }

    private static String toURIString(String identifier, String version, String suffix) {
        String base = identifier.replaceAll("/+$", "");
        String attached = suffix.replaceAll("^/+", "");

        return base + "/" + version + "/" + attached;
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
        element.setAttribute("ContentType", "application/xslt+xml");
        element.setAttribute("MediaDestinationTypeDescription", "HTML");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue", computeDigest(xsltSchema));
        element.setTextContent(buildXSLTReference());

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) { return "urn:oid:" + digestAlgorithm.getOid(); }

    // Unclear if these methods will be ever needed, but let it stay here for a while
    private Element createUsedSchemasEmbedded(String xsdSchemaContent, String xsltSchemaContent) {
        var element = document.createElement("UsedSchemasEmbedded");
        element.appendChild(createUsedXSDEmbedded(xsdSchemaContent));
        element.appendChild(createUsedPresentationSchemaEmbedded(xsltSchemaContent));

        return element;
    }

    private Element createUsedXSDEmbedded(String xsdContent) {
        var element = document.createElement("UsedXSDEmbedded");
        element.setTextContent(xsdContent);

        return element;
    }

    private Element createUsedPresentationSchemaEmbedded(String xsltContent) {
        var element = document.createElement("UsedPresentationSchemaEmbedded");
        element.setAttribute("ContentType", "application/xslt+xml");
        element.setAttribute("MediaDestinationTypeDescription", "HTML");
        element.setTextContent(xsltContent);

        return element;
    }

    private DigestAlgorithm resolveDigestMethod(String urnOidMethod) {
        if (!urnOidMethod.startsWith("urn:oid:")) throw new IllegalArgumentException();

        return DigestAlgorithm.forOID(urnOidMethod.replace("urn:oid:", ""));
    }
}
