package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;

import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.BASE64_DECODING_FAILED;

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

    public static XDCParameters empty() {
        return new XDCParameters(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public EFormAttributes getEFormAttributes(String canonicalizationMethod, DigestAlgorithm digestAlgorithm, boolean isDocumentBase64, boolean defaultAutoLoadEform) {
        return new EFormAttributes(
            identifier(),
            getTransformation(isDocumentBase64),
            getSchema(isDocumentBase64),
            containerXmlns(),
            schemaIdentifier(),
            getXsltParams(),
            getEmbedUsedSchemas(),
            getFsFormIdentifier(),
            isAutoLoadEform(defaultAutoLoadEform),
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

    public boolean isAutoLoadEform(boolean defaultValue) {
        if (autoLoadEform == null)
            return defaultValue;

        return autoLoadEform;
    }

    public boolean getEmbedUsedSchemas() {
        return getBoolean(embedUsedSchemas);
    }

    public String getSchema(boolean isDocumentBase64) {
        if (schema == null)
            return null;

        if ((schemaMimeType != null && schemaMimeType.toLowerCase().contains("base64"))
                || (schemaMimeType == null && isDocumentBase64))
            return decodeBase64(schema);

        return schema;
    }

    public boolean areResourcesBase64() {
        return schemaMimeType != null && schemaMimeType.contains("base64");
    }

    public String getTransformation(boolean isDocumentBase64) {
        if (transformation == null)
            return null;

        if ((schemaMimeType != null && schemaMimeType.toLowerCase().contains("base64"))
                || (schemaMimeType == null && isDocumentBase64))
            return decodeBase64(transformation);

        return transformation;
    }

    private static String decodeBase64(String value) {
        try {
            return new String(Base64.getDecoder().decode(value));
        } catch (IllegalArgumentException e) {
            throw new MalformedBodyException(BASE64_DECODING_FAILED);
        }
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