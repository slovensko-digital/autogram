package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.XMLValidationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.XSLT_OR_XSD_NOT_FOUND;

public abstract class EFormResources {
    protected final Charset ENCODING = StandardCharsets.UTF_8;
    protected final String url;
    protected final String xsdDigest;
    protected final String xsltDigest;
    protected final String canonicalizationMethod;
    protected String xsdIdentifier;
    protected String transformation;
    protected String xsltIdentifier;
    protected String xsltLanguage;
    protected String xsltMediaType;
    protected String xsltDestinationType;
    protected String xsltTarget;
    protected String schema;
    protected boolean embedUsedSchemas;

    protected EFormResources(String url, String xsdDigest, String xsltDigest, String canonicalizationMethod) {
        this.url = url;
        this.xsdDigest = xsdDigest;
        this.xsltDigest = xsltDigest;
        this.canonicalizationMethod = canonicalizationMethod;
    }

    public XsltParams getXsltParams() {
        return new XsltParams(xsltIdentifier, xsltLanguage, xsltDestinationType, xsltTarget, xsltMediaType);
    }

    public abstract boolean findResources() throws XMLValidationException;

    public EFormAttributes getEformAttributes() {
        var transformation = getTransformation();
        var schema = getSchema();
        if (transformation == null || schema == null)
            throw new XMLValidationException(XSLT_OR_XSD_NOT_FOUND);

        return new EFormAttributes(getIdentifier(), transformation, schema, EFormUtils.XDC_XMLNS, getXsdIdentifier(),
                getXsltParams(), shouldEmbedUsedSchemas());
    }

    public String getIdentifier() {
        return "http://data.gov.sk/doc/eform/" + url;
    }

    public String getTransformation() {
        if (!transformation.isEmpty() && transformation.charAt(0) == '\uFEFF')
            return transformation.substring(1);

        return transformation;
    }

    public String getSchema() {
        if (!schema.isEmpty() && schema.charAt(0) == '\uFEFF')
            return schema.substring(1);

        return schema;
    }

    public String getXsdIdentifier() {
        return xsdIdentifier;
    }

    public boolean shouldEmbedUsedSchemas() {
        return embedUsedSchemas;
    }
}
