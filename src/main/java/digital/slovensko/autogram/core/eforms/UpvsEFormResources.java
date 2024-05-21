package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.ManifestXsltEntry;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.util.ArrayList;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import static digital.slovensko.autogram.core.eforms.EFormUtils.computeDigest;

public class UpvsEFormResources extends EFormResources {
    private static final String SOURCE_URL = "https://data.gov.sk/doc/egov/eform/";

    public UpvsEFormResources(String url, String xsdDigest, String xsltDigest, String xsdIdentifier,
                           XsltParams xsltParams, String canonicalizationMethod) {
        super(url, xsdDigest, xsltDigest, canonicalizationMethod);

        this.xsdIdentifier = xsdIdentifier;
        this.xsltIdentifier = xsltParams != null ? xsltParams.identifier() : null;
        this.xsltLanguage = xsltParams != null ? xsltParams.language() : null;
        this.xsltMediaType = xsltParams != null ? xsltParams.mediaType() : null;
        this.xsltDestinationType = xsltParams != null ? xsltParams.destinationType() : null;
        this.xsltTarget = xsltParams != null ? xsltParams.target() : null;
        this.embedUsedSchemas = false;
    }

    public String getXsdIdentifier() {
        return xsdIdentifier != null ? xsdIdentifier : "http://schemas.gov.sk/form/" + url + "/form.xsd";
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
        var manifest_xml = getResource(SOURCE_URL + url + "/META-INF/manifest.xml");
        if (manifest_xml == null)
            throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť manifest elektronického formulára");

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
}
