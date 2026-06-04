package digital.slovensko.autogram.server.dto;

public class XDCParameters {
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
    private ServerSigningParameters.TransformationOutputMimeType transformationMediaDestinationTypeDescription;
    private String transformationTargetEnvironment;

    public String getFsFormIdentifier() {
        return fsFormIdentifier;
    }

    public boolean isAutoLoadEform() {
        return Boolean.TRUE.equals(autoLoadEform);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getContainerXmlns() {
        return containerXmlns;
    }

    public boolean getEmbedUsedSchemas() {
        return Boolean.TRUE.equals(embedUsedSchemas);
    }

    public String getSchema() {
        return schema;
    }

    public boolean areResourcesBase64() {
        return schemaMimeType != null && schemaMimeType.contains("base64");
    }

    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getTransformationIdentifier() {
        return transformationIdentifier;
    }

    public String getTransformationLanguage() {
        return transformationLanguage;
    }

    public ServerSigningParameters.TransformationOutputMimeType getTransformationMediaDestinationTypeDescription() {
        return transformationMediaDestinationTypeDescription;
    }

    public String getTransformationTargetEnvironment() {
        return transformationTargetEnvironment;
    }
}