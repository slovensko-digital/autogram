package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.ManifestXsltEntry;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.util.ArrayList;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;

public class FsEFormResources extends EFormResources {
    private static final String SOURCE_URL = "https://forms-slovensko-digital.s3.eu-central-1.amazonaws.com/fs/";
    private String xdcIdentifier;

    public FsEFormResources(String fsFormId, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
        super(fsFormId, xsdDigest, xsltDigest, canonicalizationMethod);
        this.embedUsedSchemas = false;
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

    @Override
    public boolean findResources() throws XMLValidationException {
        var meta_xml = getResource(SOURCE_URL + url + "/meta.xml");
        if (meta_xml == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť meta.xml elektronického formulára");

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
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť manifest elektronického formulára");
        }

        var parsed_manifest_xml = getXmlFromDocument(new InMemoryDocument(manifest_xml, "manifest.xml"));

        var nodes = parsed_manifest_xml.getElementsByTagName("manifest:file-entry");
        var entries = getManifestXsltEntries(nodes, SOURCE_URL, url);
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