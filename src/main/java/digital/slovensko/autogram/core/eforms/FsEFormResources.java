package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.EFormException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;

public class FsEFormResources extends EFormResources {
    private static final String SOURCE_URL = "https://forms-slovensko-digital.s3.eu-central-1.amazonaws.com/fs/";
    private String xdcIdentifier;

    public FsEFormResources(String fsFormId, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
        super(fsFormId, xsdDigest, xsltDigest, canonicalizationMethod);
        this.embedUsedSchemas = false;
    }

    public static FsEFormResources buildFromXdcIdentifier(String xdcIdentifier, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
            return new FsEFormResources(getFormIdFromXdcIdenitfier(xdcIdentifier), canonicalizationMethod, xsdDigest, xsltDigest);
    }

    private static String getFormIdFromXdcIdenitfier(String xdcIdentifier) {
        xdcIdentifier = xdcIdentifier.replaceAll("[:./ ]", "_");
        var forms_xml = getResource(SOURCE_URL + "forms.xml");
        if (forms_xml == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť zoznam FS formulárov");

        var parsed_meta_xml = getXmlFromDocument(new InMemoryDocument(forms_xml, "forms.xml"));
        var nodes = parsed_meta_xml.getElementsByTagName("sd:" + xdcIdentifier);
        if (nodes.getLength() > 0)
            return nodes.item(0).getFirstChild().getNodeValue();

        nodes = parsed_meta_xml.getElementsByTagName("sd:" + xdcIdentifier.replace("_1_0", ""));
        if (nodes.getLength() > 0)
            return nodes.item(0).getFirstChild().getNodeValue();

        nodes = parsed_meta_xml.getElementsByTagName("sd:" + xdcIdentifier + "_1_0");
        if (nodes.getLength() > 0)
            return nodes.item(0).getFirstChild().getNodeValue();

        throw new UnknownEformException();
    }

    @Override
    public boolean findResources() throws XMLValidationException, EFormException {
        var meta_xml = getResource(SOURCE_URL + url + "/meta.xml");
        if (meta_xml == null)
            throw new EFormException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť meta.xml elektronického formulára");

        var parsed_meta_xml = getXmlFromDocument(new InMemoryDocument(meta_xml, "meta.xml"));
        var nodes_meta = parsed_meta_xml.getElementsByTagName("dc:identifier");
        if (nodes_meta.getLength() < 1)
            return false;

        xdcIdentifier = nodes_meta.item(0).getFirstChild().getNodeValue();

        nodes_meta = parsed_meta_xml.getElementsByTagName("sd:xsdIdentifier");
        if (nodes_meta.getLength() < 1)
            return false;

        xsdIdentifier = nodes_meta.item(0).getFirstChild().getNodeValue();

        nodes_meta = parsed_meta_xml.getElementsByTagName("sd:xsltIdentifier");
        if (nodes_meta.getLength() < 1)
            return false;

        xsltIdentifier = nodes_meta.item(0).getFirstChild().getNodeValue();


        var manifest_xml = getResource(SOURCE_URL + url + "/META-INF/manifest.xml");
        if (manifest_xml == null) {
            throw new EFormException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť manifest elektronického formulára");
        }

        var parsed_manifest_xml = getXmlFromDocument(new InMemoryDocument(manifest_xml, "manifest.xml"));

        var nodes = parsed_manifest_xml.getElementsByTagName("manifest:file-entry");
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
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSLT transformácia sa nezhoduje s odtlačkom v XML Datacontaineri");

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
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Automaticky nájdená XSD schéma sa nezhoduje s odtlačkom v XML Datacontaineri");

        this.schema = new String(xsdString, ENCODING);

        return true;
    }

    public EFormAttributes getEformAttributes() {
        var transformation = getTransformation();
        var schema = getSchema();
        if (transformation == null || schema == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSLT transformáciu alebo XSD schému");

        return new EFormAttributes(getIdentifier(), transformation, schema, EFormUtils.XDC_XMLNS, getXsdIdentifier(), getXsltParams(), shouldEmbedUsedSchemas());
    }

    public String getIdentifier() {
        var tmp = url.split("/");
        if (tmp.length > 0)
            return xdcIdentifier + "/" + tmp[tmp.length - 1];

        return xdcIdentifier;
    }
}