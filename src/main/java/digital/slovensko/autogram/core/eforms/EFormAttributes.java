package digital.slovensko.autogram.core.eforms;

public class EFormAttributes {
    private final String identifier;
    private final String transformation;
    private final String schema;
    private final String containerXmlns;

    public EFormAttributes(String identifier, String transformation, String schema, String containerXmlns) {
        this.identifier = identifier;
        this.transformation = transformation;
        this.schema = schema;
        this.containerXmlns = containerXmlns;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getSchema() {
        return schema;
    }

    public String getContainerXmlns() {
        return containerXmlns;
    }
}
