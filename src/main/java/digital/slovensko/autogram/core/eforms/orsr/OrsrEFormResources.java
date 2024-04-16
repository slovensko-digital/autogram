package digital.slovensko.autogram.core.eforms.orsr;

import digital.slovensko.autogram.core.eforms.EFormResources;
import digital.slovensko.autogram.core.errors.XMLValidationException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.getResource;

public class OrsrEFormResources extends EFormResources {
    public OrsrEFormResources(String url, String schema, String transformation) {
        // Real url follows the space at the end of this string in orsr eforms
        super(url.replace("http://www.justice.gov.sk/Forms ", ""), null, null, null);

        this.embedUsedSchemas = true;
        this.schema = schema;
        this.transformation = transformation;
    }

    @Override
    public boolean findResources() throws XMLValidationException {
        if (schema == null) {
            var schema_raw = getResource(url);
            if (schema_raw == null)
                throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSD schému elektronického formulára");

            schema = new String(schema_raw, ENCODING);
        }

        if (transformation == null) {
            var transformationUrl = url.replace(".xsd", ".xslt");
            var transformation_raw = getResource(transformationUrl);
            if (transformation_raw == null)
                throw new XMLValidationException("Zlyhala príprava elektronického formulára", "Nepodarilo sa nájsť XSLT transformáciu elektronického formulára");

            transformation = new String(transformation_raw, ENCODING);
            if (!transformation.isEmpty() && transformation.charAt(0) == '\uFEFF')
                transformation = transformation.substring(1);

        }

        return true;
    }
}
