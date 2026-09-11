package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;

public class XDCParameters {
    private enum TransformationOutputMimeType {
        TXT,
        HTML,
        XHTML
    }

    private String fsFormIdentifier;
    private Boolean autoLoadEform;
    private String identifier;
    private String containerXmlns;
    private Boolean embedUsedSchemas;
    private String schema;
    private String schemaMimeType;
    private String schemaIdentifier;
    private String transformation;
    private String transformationIdentifier;
    private String transformationLanguage;
    private TransformationOutputMimeType transformationMediaDestinationTypeDescription;
    private String transformationTargetEnvironment;

    public EFormAttributes getEFormAttributes(String canonicalizationMethod, DigestAlgorithm digestAlgorithm) {
        return new EFormAttributes(
            getIdentifier(),
            getTransformation(),
            getSchema(),
            getContainerXmlns(),
            getSchemaIdentifier(),
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
            getTransformationIdentifier(),
            getTransformationLanguage(),
            getTransformationMediaDestinationTypeDescription(),
            getTransformationTargetEnvironment(),
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

    public String getIdentifier() {
        return identifier;
    }

    public String getContainerXmlns() {
        return containerXmlns;
    }

    public boolean getEmbedUsedSchemas() {
        return getBoolean(embedUsedSchemas);
    }

    public String getSchema() {
        if (schemaMimeType != null && schemaMimeType.toLowerCase().contains("base64"))
            return new String(Base64.getDecoder().decode(schema));

        return schema;
    }

    public boolean areResourcesBase64() {
        return schemaMimeType != null && schemaMimeType.contains("base64");
    }

    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public String getTransformation() {
        if (schemaMimeType != null && schemaMimeType.toLowerCase().contains("base64"))
            return new String(Base64.getDecoder().decode(transformation));

        return transformation;
    }

    public String getTransformationIdentifier() {
        return transformationIdentifier;
    }

    public String getTransformationLanguage() {
        return transformationLanguage;
    }

    public String getTransformationMediaDestinationTypeDescription() {
        if (transformationMediaDestinationTypeDescription == null)
            return null; 

        return transformationMediaDestinationTypeDescription.name();
    }

    public String getTransformationTargetEnvironment() {
        return transformationTargetEnvironment;
    }

    private static boolean getBoolean(Boolean variable) {
        if (variable == null)
            return false;

        return variable;
    }
}