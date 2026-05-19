package digital.slovensko.autogram.core.eforms.dto;

import digital.slovensko.autogram.core.eforms.EFormResourcesBuilder;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.EFormException;
import eu.europa.esig.dss.model.DSSDocument;

import static digital.slovensko.autogram.core.errors.EFormException.Error.MISSING_ID;
import static digital.slovensko.autogram.core.errors.EFormException.Error.XSD;
import static digital.slovensko.autogram.core.errors.EFormException.Error.XSLT;

public record EFormAttributes(String identifier, String transformation, String schema, String containerXmlns,
                              String xsdIdentifier, XsltParams xsltParams, boolean embedUsedSchemas) {

    public static EFormAttributes build(EFormAttributes eFormAttributes, boolean autoLoadEform, String fsFormId, DSSDocument document, String propertiesCanonicalization) {
        if (eFormAttributes == null)
            eFormAttributes = new EFormAttributes(null, null, null, null, null, null, false);

        return build(eFormAttributes.identifier(), eFormAttributes.transformation(), eFormAttributes.schema(),
                eFormAttributes.containerXmlns(),
                eFormAttributes.xsdIdentifier(), eFormAttributes.xsltParams(), eFormAttributes.embedUsedSchemas(),
                autoLoadEform, fsFormId, document, propertiesCanonicalization);
    }
    private static EFormAttributes build(String identifier, String transformation, String schema, String containerXmlns, String xsdIdentifier, XsltParams xsltParams, boolean embedUsedSchemas, boolean autoLoadEform, String fsFormId, DSSDocument document, String propertiesCanonicalization) {
        if (autoLoadEform || embedUsedSchemas || (fsFormId != null)) {
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

        if (transformation != null) {
            xsltParams = EFormUtils.fillXsltParams(transformation, identifier, xsltParams);
            if (!transformation.isEmpty() && transformation.charAt(0) == '\uFEFF')
                transformation = transformation.substring(1);
        }

        if (containerXmlns != null && containerXmlns.contains("xmldatacontainer")) {

            if (schema == null)
                throw new EFormException(XSD);

            if (!embedUsedSchemas && xsdIdentifier == null)
                xsdIdentifier = EFormUtils.fillXsdIdentifier(identifier);

            if (transformation == null)
                throw new EFormException(XSLT);

            if (!embedUsedSchemas && identifier == null)
                throw new EFormException(MISSING_ID);
        }

        if (EFormUtils.isOrsrUri(identifier))
            embedUsedSchemas = true;

        return new EFormAttributes(identifier, transformation, schema, containerXmlns, xsdIdentifier, xsltParams, embedUsedSchemas);
    }
}
