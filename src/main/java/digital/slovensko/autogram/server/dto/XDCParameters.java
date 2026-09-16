package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;

public record XDCParameters (
        String fsFormIdentifier, Boolean autoLoadEform, String identifier, String containerXmlns,
        Boolean embedUsedSchemas, String schema, String schemaMimeType, String schemaIdentifier,
        String transformation, String transformationIdentifier, String transformationLanguage,
        TransformationOutputMimeType transformationMediaDestinationTypeDescription,
        String transformationTargetEnvironment) {

    private enum TransformationOutputMimeType {
        TXT,
        HTML,
        XHTML
    }

    public EFormAttributes getEFormAttributes(String canonicalizationMethod, DigestAlgorithm digestAlgorithm, boolean isDocumentBase64) {
        return new EFormAttributes(
            identifier(),
            getTransformation(isDocumentBase64),
            getSchema(isDocumentBase64),
            containerXmlns(),
            schemaIdentifier(),
            getXsltParams(),
            getEmbedUsedSchemas(),
            getFsFormIdentifier(),
            isAutoLoadEform(),
            canonicalizationMethod,
            digestAlgorithm
        );
    }

    public XsltParams getXsltParams() {
        return new XsltParams(
            transformationIdentifier(),
            transformationLanguage(),
            getTransformationMediaDestinationTypeDescription(),
            transformationTargetEnvironment(),
            null
        );
    }

    public String getFsFormIdentifier() {
        var translatedFsFormId = EFormUtils.translateFsFormId(fsFormIdentifier);

        return translatedFsFormId;
    }

    public boolean isAutoLoadEform() {
        return getBoolean(autoLoadEform);
    }

    public boolean getEmbedUsedSchemas() {
        return getBoolean(embedUsedSchemas);
    }

    public String getSchema(boolean isDocumentBase64) {
        if (schema == null)
            return null;

        if (schemaMimeType != null && schemaMimeType.toLowerCase().contains("base64"))
            return new String(Base64.getDecoder().decode(schema));

        if (schemaMimeType == null && isDocumentBase64)
            return new String(Base64.getDecoder().decode(schema));

        return schema;
    }

    public boolean areResourcesBase64() {
        return schemaMimeType != null && schemaMimeType.contains("base64");
    }

    public String getTransformation(boolean isDocumentBase64) {
        if (transformation == null)
            return null;

        if (schemaMimeType != null && schemaMimeType.toLowerCase().contains("base64"))
            return new String(Base64.getDecoder().decode(transformation));

        if (schemaMimeType == null && isDocumentBase64)
            return new String(Base64.getDecoder().decode(transformation));

        return transformation;
    }

    public String getTransformationMediaDestinationTypeDescription() {
        if (transformationMediaDestinationTypeDescription == null)
            return null; 

        return transformationMediaDestinationTypeDescription.name();
    }

    private static boolean getBoolean(Boolean variable) {
        if (variable == null)
            return false;

        return variable;
    }
}