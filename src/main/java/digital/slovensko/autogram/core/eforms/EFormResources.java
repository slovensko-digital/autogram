package digital.slovensko.autogram.core.eforms;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.core.errors.MultipleOriginalDocumentsFoundException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.util.AsicContainerUtils;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import static digital.slovensko.autogram.core.AutogramMimeType.*;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import org.w3c.dom.Node;

public class EFormResources {
    private static final String SOURCE_URL = "https://test-autogram-eforms-marek.s3.eu-central-1.amazonaws.com/v1/eforms/";
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final String url;
    private final String xsdDigest;
    private final String xsltDigest;

    public static EFormAttributes tryToLoadEFormAttributes(DSSDocument document, String propertiesCanonicalization) throws AutogramException {
        if (isAsice(document.getMimeType()))
            try {
                document = AsicContainerUtils.getOriginalDocument(document);
            } catch (MultipleOriginalDocumentsFoundException | OriginalDocumentNotFoundException e) {
                return null;
            }

        if (!isXDC(document.getMimeType()) && !isXML(document.getMimeType()))
            return null;

        EFormResources eformResources;
        if (isXDC(document.getMimeType()) || XDCValidator.isXDCContent(document))
            eformResources = EFormResources.buildEFormResourcesFromXDC(document, propertiesCanonicalization);
        else
            eformResources = EFormResources.buildEFormResourcesFromEformXml(document, propertiesCanonicalization);

        if (eformResources == null)
            return null;

        var transformation = eformResources.findTransformation();
        var schema = eformResources.findSchema();
        var identifier = eformResources.getIdentifier();
        var containerXmlns = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";
        var container = ASiCContainerType.ASiC_E;

        return new EFormAttributes(identifier, transformation, schema, containerXmlns, container);
    }


    public static EFormResources buildEFormResourcesFromXDC(DSSDocument document, String canonicalizationMethod)
            throws XMLValidationException {
        var xdc = getXmlFromDocument(document).getDocumentElement();
        var formUri = getFormUri(xdc);
        var xml = getEformXmlFromXdcDocument(document);

        var xsdDigest = getDigestValueFromElement(xdc, "UsedXSDReference");
        var xsltDigest = getDigestValueFromElement(xdc, "UsedPresentationSchemaReference");

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, xsdDigest, xsltDigest);
    }

    public static EFormResources buildEFormResourcesFromEformXml(DSSDocument document, String canonicalizationMethod)
            throws XMLValidationException {
        var xml = getXmlFromDocument(document).getDocumentElement();
        var formUri = getNamespaceFromEformXml(xml);

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, null, null);
    }

    private static EFormResources buildEFormResourcesFromEformXml(Node xml, String canonicalizationMethod,
            String formUri, String xsdDigest, String xsltDigest) {
        if (formUri == null)
            return null;

        var parts = formUri.split("/");
        var formVersion = parts[parts.length - 1];
        var formIdentifier = parts[parts.length - 2];
        var formDirectory = formIdentifier + "/" + formVersion;

        return new EFormResources(formDirectory, xsdDigest, xsltDigest);
    }

    private EFormResources(String url, String xsdDigest, String xsltDigest) {
        this.url = url;
        this.xsdDigest = xsdDigest;
        this.xsltDigest = xsltDigest;
    }

    public String getIdentifier() {
        return "http://data.gov.sk/doc/eform/" + url;
    }

    public String findTransformation() throws XMLValidationException {
        var xsltString = getTransformation(SOURCE_URL + url);
        if (xsltString == null)
            return null;

        var xsltDigest = computeDigest(xsltString, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsltDigest != null && !xsltDigest.equals(this.xsltDigest))
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSLT transformácia sa nezhoduje s odtlačkom v XML Datacontaineri");

        return new String(xsltString, ENCODING);
    }

    public String findSchema() throws XMLValidationException {
        var xsdString = getSchema(SOURCE_URL + url);
        if (xsdString == null)
            return null;

        var xsdDigest = computeDigest(xsdString, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsdDigest != null && !xsdDigest.equals(this.xsdDigest))
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSD schéma sa nezhoduje s odtlačkom v XML Datacontaineri");

        return new String(xsdString, ENCODING);
    }

}
