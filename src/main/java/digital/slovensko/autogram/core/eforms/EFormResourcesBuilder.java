package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.core.errors.XMLValidationException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import static digital.slovensko.autogram.core.AutogramMimeType.*;

import eu.europa.esig.dss.model.DSSDocument;

import org.w3c.dom.Element;

public abstract class EFormResourcesBuilder {
    public static EFormResources build(DSSDocument document, String fsFormId, String xsdIdentifier, XsltParams xsltParams, String propertiesCanonicalization) {
        var documentMimeType = document.getMimeType();
        EFormResources eformResources = null;
        if (isXDC(documentMimeType))
            eformResources = tryToBuildFromEmbeddedXdc(document);

        if ((isXML(documentMimeType) || isXDC(documentMimeType)) && (eformResources == null))
            return buildFromDocument(document, fsFormId, propertiesCanonicalization, xsdIdentifier, xsltParams);

        return null;
    }

    public static EFormResources buildFromDocument(DSSDocument document, String fsFormId, String propertiesCanonicalization, String xsdIdentifier, XsltParams xsltParams)
            throws AutogramException {

        var xml = getXmlFromDocument(document).getDocumentElement();
        EFormResources eformResources;
        if (isXDC(document.getMimeType()))
            eformResources = buildFromXDC(xml, propertiesCanonicalization);
        else
            eformResources = buildFromEFormXml(xml, propertiesCanonicalization, xsdIdentifier, xsltParams, fsFormId);

        if (eformResources == null)
            return null;

        if (!eformResources.findResources())
            return null;

        return eformResources;
    }

    public static EFormResources tryToBuildFromEmbeddedXdc(DSSDocument document) {
        try {
            var xdc = getXmlFromDocument(document).getDocumentElement();
            var formUri = getFormUri(xdc);
            if (formUri == null)
                return null;

            var schemaNode = (Element) getNoTextFirstChild(getElementFromXdc(xdc, "UsedXSDEmbedded"));
            var schema = transformElementToString(schemaNode);
            var transformationNode = (Element) getNoTextFirstChild(getElementFromXdc(xdc, "UsedPresentationSchemaEmbedded"));
            var transformation = transformElementToString(transformationNode);
            if (transformation == null || schema == null)
                return null;

            return new OrsrEFormResources(formUri, schema, transformation);

        } catch (XMLValidationException e) {
            return null;
        }
    }

    private static EFormResources buildFromXDC(Element xdc, String canonicalizationMethod)
            throws XMLValidationException, UnknownEformException {
        var xsdDigest = getDigestValueFromElement(xdc, "UsedXSDReference");
        var xsltDigest = getDigestValueFromElement(xdc, "UsedPresentationSchemaReference");
        var formUri = getFormUri(xdc);
        var xsdIdentifier = getValueFromElement(xdc, "UsedXSDReference");
        var params = getXsltParamsFromXsltReference(xdc);

        return buildFromEFormXml(canonicalizationMethod, formUri, xsdDigest, xsltDigest, xsdIdentifier, params);
    }

    private static EFormResources buildFromEFormXml(Element xml, String canonicalizationMethod, String xsdIdentifier, XsltParams xsltParams, String fsFormId)
            throws XMLValidationException, UnknownEformException {
        if (fsFormId != null)
            return FsEFormResources.buildFromFsFormId(fsFormId, canonicalizationMethod, null, null);

        var formUri = getNamespaceFromEformXml(xml);
        if (isOrsrUri(formUri))
            return new OrsrEFormResources(formUri, null, null);

        return buildFromEFormXml(canonicalizationMethod, formUri, null, null, xsdIdentifier, xsltParams);
    }

    private static EFormResources buildFromEFormXml(String canonicalizationMethod, String formUri, String xsdDigest, String xsltDigest, String xsdIdentifier, XsltParams xsltParams)
            throws XMLValidationException, UnknownEformException {
        if (formUri == null)
            return null;

        if (formUri.startsWith("http://www.drsr.sk/") || formUri.startsWith("https://ekr.financnasprava.sk/"))
            return FsEFormResources.buildFromXdcIdentifier(formUri, canonicalizationMethod, xsdDigest, xsltDigest);

        if (formUri.startsWith("http://schemas.gov.sk/form/") || formUri.startsWith("http://data.gov.sk/doc/eform/") || formUri.startsWith("https://data.gov.sk/id/egov/eform/")) {
            var parts = formUri.split("/");
            var formVersion = parts[parts.length - 1];
            var formIdentifier = parts[parts.length - 2];

            return new UpvsEFormResources(formIdentifier + "/" + formVersion, xsdDigest, xsltDigest, xsdIdentifier, xsltParams, canonicalizationMethod);
        }

        return null;
    }
}
