package digital.slovensko.autogram.core.eforms;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.InvalidXMLException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import org.w3c.dom.Node;

public class EFormResources {
    private static final String SOURCE_URL = "https://test-autogram-eforms-marek.s3.eu-central-1.amazonaws.com/v1/eforms/";
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final String url;
    private final String xsdDigest;
    private final String xsltDigest;

    public static EFormResources buildEFormResourcesFromXDC(DSSDocument document, String canonicalizationMethod)
            throws InvalidXMLException {
        var xdc = getXmlFromDocument(document).getDocumentElement();
        var formUri = getFormUri(xdc);
        var xml = getEformXmlFromXdcDocument(document);

        var xsdDigest = getDigestValueFromElement(xdc, "UsedXSDReference");
        var xsltDigest = getDigestValueFromElement(xdc, "UsedPresentationSchemaReference");

        return buildEFormResourcesFromEformXml(xml, canonicalizationMethod, formUri, xsdDigest, xsltDigest);
    }

    public static EFormResources buildEFormResourcesFromEformXml(DSSDocument document, String canonicalizationMethod)
            throws InvalidXMLException {
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

    public String findTransformation() throws AutogramException {
        var xsltString = getTransformation(SOURCE_URL + url);
        if (xsltString == null)
            return null;

        var xsltDigest = computeDigest(xsltString, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsltDigest != null && !xsltDigest.equals(this.xsltDigest))
            throw new AutogramException("XSLT digest mismatch", "XSLT digest mismatch",
                    "XSLT digest mismatch: " + xsltDigest + " != " + this.xsltDigest);

        return new String(xsltString, ENCODING);
    }

    public String findSchema() throws AutogramException {
        var xsdString = getSchema(SOURCE_URL + url);
        if (xsdString == null)
            return null;

        var xsdDigest = computeDigest(xsdString, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsdDigest != null && !xsdDigest.equals(this.xsdDigest))
            throw new AutogramException("XSD digest mismatch", "XSD digest mismatch",
                    "XSD digest mismatch: " + xsdDigest + " != " + this.xsdDigest);

        return new String(xsdString, ENCODING);
    }

}
