package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.EFormException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.apache.xml.security.utils.DOMNamespaceContext;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static digital.slovensko.autogram.core.eforms.EFormUtils.computeDigest;
import static digital.slovensko.autogram.core.eforms.EFormUtils.getManifestXsltEntries;
import static digital.slovensko.autogram.core.eforms.EFormUtils.getResource;
import static digital.slovensko.autogram.core.eforms.EFormUtils.getXmlFromDocument;
import static digital.slovensko.autogram.core.eforms.EFormUtils.selectXslt;
import static digital.slovensko.autogram.core.errors.EFormException.Error.MANIFEST;
import static digital.slovensko.autogram.core.errors.EFormException.Error.META_XML;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.AUTO_XSD_DIGEST_MISMATCH;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.AUTO_XSLT_DIGEST_MISMATCH;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.FORMS_NOT_FOUND;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.XSLT_OR_XSD_NOT_FOUND;

public class FsEFormResources extends EFormResources {
    private static final String SOURCE_URL = "https://forms-slovensko-digital.s3.eu-central-1.amazonaws.com/fs/";
    private String xdcIdentifier;

    private FsEFormResources(String formUrl, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
        super(formUrl, xsdDigest, xsltDigest, canonicalizationMethod);
        this.embedUsedSchemas = false;
    }

    public static FsEFormResources buildFromFsFormId(String fsFormId, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
        return new FsEFormResources(getFormUrlFromFsFormId(fsFormId), canonicalizationMethod, xsdDigest, xsltDigest);
    }

    public static FsEFormResources buildFromXdcIdentifier(String xdcIdentifier, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
        return new FsEFormResources(getFormUrlFromXdcIdentifier(xdcIdentifier), canonicalizationMethod, xsdDigest, xsltDigest);
    }

    private static String getFormUrlFromFsFormId(String fsFormId) {
        var forms_xml = getResource(SOURCE_URL + "forms.xml");
        if (forms_xml == null)
            throw new XMLValidationException(FORMS_NOT_FOUND);

        var parsed_meta_xml = getXmlFromDocument(new InMemoryDocument(forms_xml, "forms.xml"));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var nsContext = new DOMNamespaceContext(parsed_meta_xml);
            xpath.setNamespaceContext(nsContext);
            var ns = nsContext.getPrefix("urn:meta.slovensko.digital:1.0");

            var r = getSlugAndVersion(xpath, parsed_meta_xml, "//" + ns + ":form[@sdIdentifier=\"" + fsFormId + "\"]");
            if (r != null)
                return r;

        } catch (XPathExpressionException e) {
            throw new UnrecognizedException(e);
        }

        throw new UnknownEformException();

    }

    private static String getFormUrlFromXdcIdentifier(String xdcIdentifier) {
        var forms_xml = getResource(SOURCE_URL + "forms.xml");
        if (forms_xml == null)
            throw new XMLValidationException(FORMS_NOT_FOUND);

        var parsed_meta_xml = getXmlFromDocument(new InMemoryDocument(forms_xml, "forms.xml"));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var nsContext = new DOMNamespaceContext(parsed_meta_xml);
            xpath.setNamespaceContext(nsContext);
            var ns = nsContext.getPrefix("urn:meta.slovensko.digital:1.0");

            var r = getSlugAndVersion(xpath, parsed_meta_xml, "//" + ns + ":form[@xdcIdentifier=\"" + xdcIdentifier + "\"]");
            if (r != null)
                return r;

            r = getSlugAndVersion(xpath, parsed_meta_xml, "//" + ns + ":form[@xdcIdentifier=\"" + xdcIdentifier + "/1.0\"]");
            if (r != null)
                return r;

            r = getSlugAndVersion(xpath, parsed_meta_xml, "//" + ns + ":form[@xdcIdentifier=\"" + xdcIdentifier.replace("/1.0", "") + "\"]");
            if (r != null)
                return r;

        } catch (XPathExpressionException e) {
            throw new UnrecognizedException(e);
        }

        throw new UnknownEformException();
    }

    private static String getSlugAndVersion(XPath xpath, Document parsed_xml, String query) throws XPathExpressionException {
        var n = xpath.compile(query + "/@slug").evaluate(parsed_xml);
        if (!n.isEmpty() && !n.equals("NaN"))
            return n + "/" + xpath.compile(query + "/@version").evaluate(parsed_xml);

        return null;
    }

    @Override
    public boolean findResources() throws XMLValidationException, EFormException {
        var meta_xml = getResource(SOURCE_URL + url + "/meta.xml");
        if (meta_xml == null)
            throw new EFormException(META_XML);

        var parsed_meta_xml = getXmlFromDocument(new InMemoryDocument(meta_xml, "meta.xml"));
        var nodes_meta = parsed_meta_xml.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "identifier");
        if (nodes_meta.getLength() < 1)
            return false;

        xdcIdentifier = nodes_meta.item(0).getFirstChild().getNodeValue();

        nodes_meta = parsed_meta_xml.getElementsByTagNameNS("urn:meta.slovensko.digital:1.0","xsdIdentifier");
        if (nodes_meta.getLength() < 1)
            return false;

        xsdIdentifier = nodes_meta.item(0).getFirstChild().getNodeValue();

        nodes_meta = parsed_meta_xml.getElementsByTagNameNS("urn:meta.slovensko.digital:1.0","xsltIdentifier");
        if (nodes_meta.getLength() < 1)
            return false;

        xsltIdentifier = nodes_meta.item(0).getFirstChild().getNodeValue();


        var manifest_xml = getResource(SOURCE_URL + url + "/META-INF/manifest.xml");
        if (manifest_xml == null) {
            throw new EFormException(MANIFEST);
        }

        var parsed_manifest_xml = getXmlFromDocument(new InMemoryDocument(manifest_xml, "manifest.xml"));

        var nodes = parsed_manifest_xml.getElementsByTagNameNS("urn:manifest:1.0", "file-entry");
        var entries = getManifestXsltEntries(nodes, SOURCE_URL, url);
        if (entries.isEmpty())
            return false;

        var entry = selectXslt(entries, xsltDestinationType, xsltLanguage, xsltTarget);
        if (entry == null)
            return false;

        var xsltString = getResource(SOURCE_URL + url + "/" + entry.fullPath());
        if (xsltString == null)
            return false;

        var xsltDigest = computeDigest(xsltString, canonicalizationMethod, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsltDigest != null && !xsltDigest.equals(this.xsltDigest))
            throw new XMLValidationException(AUTO_XSLT_DIGEST_MISMATCH);

        transformation = new String(xsltString, ENCODING);
        if (!transformation.isEmpty() && transformation.charAt(0) == '\uFEFF')
            transformation = transformation.substring(1);

        this.xsltDestinationType = entry.destinationType();
        this.xsltLanguage = entry.language();
        this.xsltMediaType = entry.mediaType();
        this.xsltTarget = entry.target();

        var xsdString = getResource(SOURCE_URL + url + "/schema.xsd");
        if (xsdString == null)
            return false;

        var xsdDigest = computeDigest(xsdString, canonicalizationMethod, DigestAlgorithm.SHA256, ENCODING);
        if (this.xsdDigest != null && !xsdDigest.equals(this.xsdDigest))
            throw new XMLValidationException(AUTO_XSD_DIGEST_MISMATCH);

        this.schema = new String(xsdString, ENCODING);

        return true;
    }

    public EFormAttributes getEformAttributes() {
        var transformation = getTransformation();
        var schema = getSchema();
        if (transformation == null || schema == null)
            throw new XMLValidationException(XSLT_OR_XSD_NOT_FOUND);

        return new EFormAttributes(getIdentifier(), transformation, schema, EFormUtils.XDC_XMLNS, getXsdIdentifier(), getXsltParams(), shouldEmbedUsedSchemas());
    }

    public String getIdentifier() {
        if (xdcIdentifier.matches(".*/[v0-9.]+$"))
            return xdcIdentifier;

        return xdcIdentifier + "/1.0";
    }
}