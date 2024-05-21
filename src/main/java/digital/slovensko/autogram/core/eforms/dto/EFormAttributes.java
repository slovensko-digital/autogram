package digital.slovensko.autogram.core.eforms.dto;

import digital.slovensko.autogram.core.eforms.EFormResourcesBuilder;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;

public record EFormAttributes(String identifier, String transformation, String schema, String containerXmlns,
                              String xsdIdentifier, XsltParams xsltParams, boolean embedUsedSchemas) {

    public static EFormAttributes build(EFormAttributes eFormAttributes, ASiCContainerType container, SignaturePackaging packaging, boolean autoLoadEform, String fsFormId, DSSDocument document, String propertiesCanonicalization) {
        if (eFormAttributes == null)
            eFormAttributes = new EFormAttributes(null, null, null, null, null, null, false);

        return build(eFormAttributes.identifier(), eFormAttributes.transformation(), eFormAttributes.schema(),
                eFormAttributes.containerXmlns(), container, packaging,
                eFormAttributes.xsdIdentifier(), eFormAttributes.xsltParams(), eFormAttributes.embedUsedSchemas(),
                autoLoadEform, fsFormId, document, propertiesCanonicalization);
    }
    private static EFormAttributes build(String identifier, String transformation, String schema, String containerXmlns, ASiCContainerType container, SignaturePackaging packaging, String xsdIdentifier, XsltParams xsltParams, boolean embedUsedSchemas, boolean autoLoadEform, String fsFormId, DSSDocument document, String propertiesCanonicalization) {
        if (autoLoadEform || (fsFormId != null)) {
            var eFormResources = EFormResourcesBuilder.build(document, fsFormId, xsdIdentifier, xsltParams, propertiesCanonicalization);
            if (eFormResources != null) {
                var loadedEFormAttributes = eFormResources.getEformAttributes();
                if (loadedEFormAttributes != null) {
                    schema = loadedEFormAttributes.schema();
                    transformation = loadedEFormAttributes.transformation();
                    identifier = loadedEFormAttributes.identifier();
                    containerXmlns = loadedEFormAttributes.containerXmlns();
                    xsdIdentifier = loadedEFormAttributes.xsdIdentifier();
                    xsltParams = loadedEFormAttributes.xsltParams();
                    embedUsedSchemas |= loadedEFormAttributes.embedUsedSchemas();
                }
            }
        }

        if (transformation != null)
            xsltParams = EFormUtils.fillXsltParams(transformation, identifier, xsltParams);

        if (containerXmlns != null && containerXmlns.contains("xmldatacontainer")) {

            if (schema == null)
                throw new SigningParametersException("Chýba XSD schéma", "XSD Schéma je povinný atribút pre XML Datacontainer");

            if (!embedUsedSchemas && xsdIdentifier == null)
                xsdIdentifier = EFormUtils.fillXsdIdentifier(identifier);

            if (transformation == null)
                throw new SigningParametersException("Chýba XSLT transformácia", "XSLT transformácia je povinný atribút pre XML Datacontainer");

            if (!embedUsedSchemas && identifier == null)
                throw new SigningParametersException("Chýba identifikátor", "Identifikátor je povinný atribút pre XML Datacontainer");
        }

        return new EFormAttributes(identifier, transformation, schema, containerXmlns, xsdIdentifier, xsltParams, embedUsedSchemas);
    }
}
