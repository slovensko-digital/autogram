package digital.slovensko.autogram.core.eforms;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import digital.slovensko.autogram.core.eforms.dto.ManifestXsltEntry;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.XMLValidationException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import static digital.slovensko.autogram.core.AutogramMimeType.*;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EFormResources {
    private static final String SOURCE_URL = "https://data.gov.sk/doc/egov/eform/";
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final String url;
    private final String xsdDigest;
    private final String xsltDigest;
    private final String canonicalizationMethod;
    private String xsdIdentifier;
    private String transformation;
    private String xsltIdentifier;
    private String xsltLanguage;
    private String xsltMediaType;
    private String xsltDestinationType;
    private String xsltTarget;
    private String schema;
    private boolean embedUsedSchemas;

    public static EFormAttributes tryToLoadEFormAttributes(DSSDocument document, String propertiesCanonicalization,
            String xsdIdentifier, XsltParams xsltParams)
            throws AutogramException {
        if (!isXDC(document.getMimeType()) && !isXML(document.getMimeType()))
            return null;

        EFormResources eformResources;
        if (isXDC(document.getMimeType()) || XDCValidator.isXDCContent(document))
            eformResources = EFormResources.buildEFormResourcesFromXDC(document, propertiesCanonicalization);
        else
            eformResources = EFormResources.buildEFormResourcesFromEformXml(document, propertiesCanonicalization,
                    xsdIdentifier, xsltParams);

        if (eformResources == null)
            return null;

        if (!eformResources.findResources())
            return null;

        var transformation = eformResources.getTransformation();
        var schema = eformResources.getSchema();
        if (transformation == null || schema == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSLT transformáciu alebo XSD schému");

        var identifier = eformResources.getIdentifier();
        var containerXmlns = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";
        var container = ASiCContainerType.ASiC_E;
        var packaging = SignaturePackaging.ENVELOPING;
        xsdIdentifier = eformResources.getXsdIdentifier();
        xsltParams = eformResources.getXsltParams();
        var embedUsedSchemas = eformResources.shouldEmbedUsedSchemas();

        return new EFormAttributes(identifier, transformation, schema, containerXmlns, container, packaging,
                xsdIdentifier, xsltParams, embedUsedSchemas);
    }

    private boolean shouldEmbedUsedSchemas() {
        return embedUsedSchemas;
    }

    private ArrayList<ManifestXsltEntry> getManifestXsltEntries(NodeList nodes) {
        var entries = new ArrayList<ManifestXsltEntry>();

        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);

            var fullPathNode = node.getAttributes().getNamedItem("full-path");
            if (fullPathNode == null)
                continue;
            var fullPath = fullPathNode.getNodeValue().replace("\\", "/");

            var mediaTypeNode = node.getAttributes().getNamedItem("media-type");
            var mediaType = mediaTypeNode != null ? mediaTypeNode.getNodeValue() : null;

            var mediaDestination = "";
            var mediaDestinationNode = node.getAttributes().getNamedItem("media-destination");
            if (mediaDestinationNode != null) {
                mediaDestination = mediaDestinationNode.getNodeValue();
                if(!mediaDestination.equals("sign") && !mediaDestination.equals("view"))
                    continue;

                if (mediaType == null)
                    continue;

                if (!mediaType.equals("application/xslt+xml") && !mediaType.equals("text/xsl"))
                    if (!(
                            (mediaType.equals("text/xml") || mediaDestination.equals("application/xml"))
                            && (fullPath.contains(".xsl") || fullPath.contains(".xslt"))
                        ))
                        continue;

            } else {
                if (!fullPath.contains(".sb.xslt") && !fullPath.contains(".html.xslt"))
                    continue;

                mediaDestination = fullPath.contains(".sb.xslt") ? "sign" : "view";
            }

            var languageNode = node.getAttributes().getNamedItem("media-language");
            var language = languageNode != null ? languageNode.getNodeValue() : null;

            var mediaDestinationTypeDescriptionNode = node.getAttributes().getNamedItem("media-destination-type-description");
            var mediaDestinationTypeDescription = mediaDestinationTypeDescriptionNode != null ? mediaDestinationTypeDescriptionNode.getNodeValue() : null;

            if (mediaDestinationTypeDescription == null) {
                var mediaDestinationTypeNode = node.getAttributes().getNamedItem("media-destination-type");
                var mediaDestinationType = mediaDestinationTypeNode != null ? mediaDestinationTypeNode.getNodeValue() : null;

                if (mediaDestinationType != null)
                    mediaDestinationTypeDescription = switch (mediaDestinationType) {
                        case "text/plain" -> "TXT";
                        case "text/html" -> "HTML";
                        case "application/xhtml+xml" -> "XHTML";
                        default -> null;
                    };
            }

            if (mediaDestinationTypeDescription == null) {
                // need to get output method from xslt
                var xsltString = getResource(SOURCE_URL + url + "/" + fullPath);
                if (xsltString == null)
                    continue;

                try {
                    mediaDestinationTypeDescription = EFormUtils.extractTransformationOutputMimeTypeString(new String(xsltString, ENCODING));
                } catch (TransformationParsingErrorException e) {
                    continue;
                }
            }

            var targetEnvironmentNode = node.getAttributes().getNamedItem("target-environment");
            var targetEnvironment = targetEnvironmentNode != null ? targetEnvironmentNode.getNodeValue() : null;

            entries.add(new ManifestXsltEntry(mediaType, language, mediaDestinationTypeDescription, targetEnvironment,
                    fullPath, mediaDestination));
        }

        return entries;
    }

    private ManifestXsltEntry selectXslt(ArrayList<ManifestXsltEntry> entries) {
        if (xsltDestinationType != null)
            entries.removeIf(entry -> !xsltDestinationType.equals(entry.destinationType()));

        if (xsltLanguage != null)
            entries.removeIf(entry -> !xsltLanguage.equals(entry.language()));

        if (xsltTarget != null)
            entries.removeIf(entry -> !xsltTarget.equals(entry.target()));

        if (entries.size() == 1)
            return entries.get(0);

        if (entries.stream().filter(entry -> entry.mediaDesination().equals("sign")).count() > 0)
            entries.removeIf(entry -> !entry.mediaDesination().equals("sign"));

        if (entries.stream().filter(entry -> entry.destinationType().equals("XHTML")).count() > 0)
            entries.removeIf(entry -> !entry.destinationType().equals("XHTML"));

        else if (entries.stream().filter(entry -> entry.destinationType().equals("HTML")).count() > 0)
            entries.removeIf(entry -> !entry.destinationType().equals("HTML"));

        else if (entries.stream().filter(entry -> entry.destinationType().equals("TXT")).count() > 0)
            entries.removeIf(entry -> !entry.destinationType().equals("TXT"));

        if (entries.stream().filter(entry -> entry.language().equals("sk")).count() > 0)
            entries.removeIf(entry -> !entry.language().equals("sk"));

        else if (entries.stream().filter(entry -> entry.language().equals("en")).count() > 0)
            entries.removeIf(entry -> !entry.language().equals("en"));

        return entries.get(0);
    }

    private boolean findResources() throws XMLValidationException {
        if (embedUsedSchemas)
            return findResourcesEmbedded();

        var manifest_xml = getResource(SOURCE_URL + url + "/META-INF/manifest.xml");
        if (manifest_xml == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť manifest elektronického formulára");

        var parsed_manifest_xml = getXmlFromDocument(new InMemoryDocument(manifest_xml, "manifest.xml"));
        var nodes = parsed_manifest_xml.getElementsByTagName("manifest:file-entry");

        var entries = getManifestXsltEntries(nodes);
        if (entries.isEmpty())
            return false;

        var entry = selectXslt(entries);
        if (entry == null)
            return false;

        var xsltString = getResource(SOURCE_URL + url + "/" + entry.fullPath());
        if (xsltString == null)
            return false;

        var xsltDigest = computeDigest(xsltString, canonicalizationMethod, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsltDigest != null && !xsltDigest.equals(this.xsltDigest))
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSLT transformácia sa nezhoduje s odtlačkom v XML Datacontaineri");

        this.transformation = new String(xsltString, ENCODING);
        this.xsltDestinationType = entry.destinationType();
        this.xsltLanguage = entry.language();
        this.xsltMediaType = entry.mediaType();
        this.xsltTarget = entry.target();
        if (this.xsltIdentifier == null)
            this.xsltIdentifier = "http://schemas.gov.sk/form/" + url + "/form.xslt";

        var xsdString = getResource(SOURCE_URL + url + "/schema.xsd");
        if (xsdString == null)
            return false;

        var xsdDigest = computeDigest(xsdString, canonicalizationMethod, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsdDigest != null && !xsdDigest.equals(this.xsdDigest))
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSD schéma sa nezhoduje s odtlačkom v XML Datacontaineri");

        this.schema = new String(xsdString, ENCODING);

        return true;
    }

    private boolean findResourcesEmbedded() throws XMLValidationException {
        if (schema == null) {
            var schema_raw = getResource(url);
            if (schema_raw == null)
                throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSD schému elektronického formulára");

            schema = new String(schema_raw, ENCODING);
        }

        if (transformation == null) {
            var transformationUrl = url.replace(".xsd", ".xslt");
            var transformation_raw = getResource(transformationUrl);
            if (transformation_raw == null)
                throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSLT transformáciu elektronického formulára");

            transformation = new String(transformation_raw, ENCODING);
            if (!transformation.isEmpty() && transformation.charAt(0) == '\uFEFF')
                transformation = transformation.substring(1);

        }

        return true;
    }

    private XsltParams getXsltParams() {
        return new XsltParams(xsltIdentifier, xsltLanguage, xsltDestinationType, xsltTarget, xsltMediaType);
    }

    private String getXsdIdentifier() {
        return xsdIdentifier != null ? xsdIdentifier : "http://schemas.gov.sk/form/" + url + "/form.xsd";
    }

    private static boolean isOrsrUri(String uri) {
        return uri != null && uri.contains("://eformulare.justice.sk");
    }

    private static EFormResources buildEFormResourcesFromXDC(DSSDocument document, String canonicalizationMethod)
            throws XMLValidationException {
        var xdc = getXmlFromDocument(document).getDocumentElement();
        var formUri = getFormUri(xdc);

        if (isOrsrUri(formUri)) {
            var schemaNode = (Element) getElementFromXdc(xdc, "UsedXSDEmbedded").getFirstChild();
            var schema = transformElementToString(schemaNode);
            var transformationNode = (Element) getElementFromXdc(xdc, "UsedPresentationSchemaEmbedded").getFirstChild();
            var transformation = transformElementToString(transformationNode);

            return new EFormResources(formUri, schema, transformation);
        }

        var xml = getEformXmlFromXdcDocument(document);

        var xsdDigest = getDigestValueFromElement(xdc, "UsedXSDReference");
        var xsltDigest = getDigestValueFromElement(xdc, "UsedPresentationSchemaReference");

        var xsdIdentifier = getValueFromElement(xdc, "UsedXSDReference");

        var params = getXsltParamsFromXsltReference(xdc);

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, xsdDigest, xsltDigest, xsdIdentifier, params);
    }

    private static EFormResources buildEFormResourcesFromEformXml(DSSDocument document, String canonicalizationMethod,
            String xsdIdentifier, XsltParams xsltParams)
            throws XMLValidationException {
        var xml = getXmlFromDocument(document).getDocumentElement();
        var formUri = getNamespaceFromEformXml(xml);

        if (isOrsrUri(formUri))
            return new EFormResources(formUri, null, null);

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, null, null, xsdIdentifier,
                xsltParams);
    }

    private static EFormResources buildEFormResourcesFromEformXml(Node xml, String canonicalizationMethod,
            String formUri, String xsdDigest, String xsltDigest, String xsdIdentifier, XsltParams xsltParams) {
        if (formUri == null)
            return null;

        if (!formUri.startsWith("http://schemas.gov.sk/form/") && !formUri.startsWith("http://data.gov.sk/doc/eform/") && !formUri.startsWith("https://data.gov.sk/id/egov/eform/"))
            return null;

        var parts = formUri.split("/");
        var formVersion = parts[parts.length - 1];
        var formIdentifier = parts[parts.length - 2];
        var formDirectory = formIdentifier + "/" + formVersion;

        return new EFormResources(formDirectory, xsdDigest, xsltDigest, xsdIdentifier, xsltParams, canonicalizationMethod);
    }

    private EFormResources(String url, String xsdDigest, String xsltDigest, String xsdIdentifier,
            XsltParams xsltParams, String canonicalizationMethod) {
        this.url = url;
        this.xsdDigest = xsdDigest;
        this.xsltDigest = xsltDigest;
        this.xsdIdentifier = xsdIdentifier;
        this.xsltIdentifier = xsltParams != null ? xsltParams.identifier() : null;
        this.xsltLanguage = xsltParams != null ? xsltParams.language() : null;
        this.xsltMediaType = xsltParams != null ? xsltParams.mediaType() : null;
        this.xsltDestinationType = xsltParams != null ? xsltParams.destinationType() : null;
        this.xsltTarget = xsltParams != null ? xsltParams.target() : null;
        this.canonicalizationMethod = canonicalizationMethod;
        this.embedUsedSchemas = false;
    }

    private EFormResources(String url, String schema, String transformation) {
        this.embedUsedSchemas = true;

        // Real url follows the space at the end of this string in orsr eforms
        this.url = url.replace("http://www.justice.gov.sk/Forms ", "");
        this.schema = schema;
        this.transformation = transformation;
        this.xsdDigest = null;
        this.xsltDigest = null;
        this.canonicalizationMethod = null;
    }

    private String getIdentifier() {
        return "http://data.gov.sk/doc/eform/" + url;
    }

    private String getTransformation() {
        return transformation;
    }

    private String getSchema() {
        return schema;
    }

}
