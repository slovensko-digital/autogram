package digital.slovensko.autogram.core.eforms;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.XMLValidationException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import static digital.slovensko.autogram.core.AutogramMimeType.*;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import org.w3c.dom.Node;

public class EFormResources {
    private static final String SOURCE_URL = "https://data.gov.sk/doc/egov/eform/";
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final String url;
    private final String xsdDigest;
    private final String xsltDigest;

    public static EFormAttributes tryToLoadEFormAttributes(DSSDocument document, String propertiesCanonicalization) throws AutogramException {
        if (!isXDC(document.getMimeType()) && !isXML(document.getMimeType()))
            return null;

        EFormResources eformResources;
        if (isXDC(document.getMimeType()) || XDCValidator.isXDCContent(document))
            eformResources = EFormResources.buildEFormResourcesFromXDC(document, propertiesCanonicalization);
        else
            eformResources = EFormResources.buildEFormResourcesFromEformXml(document, propertiesCanonicalization);

        if (eformResources == null)
            return null;

        var transformation = eformResources.getTransformation();
        var schema = eformResources.getSchema();
        if (transformation == null || schema == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSLT transformáciu alebo XSD schému");

        var identifier = eformResources.getIdentifier();
        var containerXmlns = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";
        var container = ASiCContainerType.ASiC_E;

        return new EFormAttributes(identifier, transformation, schema, containerXmlns, container);
    }


    private static EFormResources buildEFormResourcesFromXDC(DSSDocument document, String canonicalizationMethod)
            throws XMLValidationException {
        var xdc = getXmlFromDocument(document).getDocumentElement();
        var formUri = getFormUri(xdc);
        var xml = getEformXmlFromXdcDocument(document);

        var xsdDigest = getDigestValueFromElement(xdc, "UsedXSDReference");
        var xsltDigest = getDigestValueFromElement(xdc, "UsedPresentationSchemaReference");

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, xsdDigest, xsltDigest);
    }

    private static EFormResources buildEFormResourcesFromEformXml(DSSDocument document, String canonicalizationMethod)
            throws XMLValidationException {
        var xml = getXmlFromDocument(document).getDocumentElement();
        var formUri = getNamespaceFromEformXml(xml);

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, null, null);
    }

    private static EFormResources buildEFormResourcesFromEformXml(Node xml, String canonicalizationMethod,
            String formUri, String xsdDigest, String xsltDigest) {
        if (formUri == null || !(formUri.startsWith("http://schemas.gov.sk/form/") || formUri.startsWith("http://data.gov.sk/doc/eform/")))
            return null;

        var parts = formUri.split("/");
        var formVersion = parts[parts.length - 1];
        var formIdentifier = parts[parts.length - 2];
        var formDirectory = SOURCE_URL + formIdentifier + "/" + formVersion;

        return new EFormResources(formDirectory, xsdDigest, xsltDigest);
    }

    private EFormResources(String url, String xsdDigest, String xsltDigest) {
        this.url = url;
        this.xsdDigest = xsdDigest;
        this.xsltDigest = xsltDigest;
    }

    private String getIdentifier() {
        return "http://data.gov.sk/doc/eform/" + url;
    }

    private String getTransformation() throws XMLValidationException {
        var manifest_xml = getResource(url + "/META-INF/manifest.xml");
        if (manifest_xml == null)
            return null;

        var fullPath = "";
        try {
            var parsed_manifest_xml = getXmlFromDocument(new InMemoryDocument(manifest_xml, "manifest.xml"));
            var nodes = parsed_manifest_xml.getElementsByTagName("manifest:file-entry");
            for (int i = 0; i < nodes.getLength(); i++) {
                var node = nodes.item(i);

                var mediaDestination = node.getAttributes().getNamedItem("media-destination");
                if (mediaDestination != null && mediaDestination.getNodeValue().equals("sign")) {
                    fullPath = node.getAttributes().getNamedItem("full-path").getNodeValue();

                    fullPath = fullPath.replace("\\", "/");
                    if (fullPath != null && fullPath.length() > 0)
                        break;
                }
            }

        } catch (XMLValidationException e) {
            return null;
        }

        if (fullPath == null || fullPath.length() == 0)
            return null;

        var xsltString = getResource(url + "/" + fullPath);
        if (xsltString == null)
            return null;

        var xsltDigest = computeDigest(xsltString, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsltDigest != null && !xsltDigest.equals(this.xsltDigest))
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSLT transformácia sa nezhoduje s odtlačkom v XML Datacontaineri");

        return new String(xsltString, ENCODING);
    }

    private String getSchema() throws XMLValidationException {
        var xsdString = getResource(url + "/schema.xsd");
        if (xsdString == null)
            return null;

        var xsdDigest = computeDigest(xsdString, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsdDigest != null && !xsdDigest.equals(this.xsdDigest))
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSD schéma sa nezhoduje s odtlačkom v XML Datacontaineri");

        return new String(xsdString, ENCODING);
    }

}
