package digital.slovensko.autogram.core;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

public class XMLValidator {

    private String xmlContent;

    private String xsdSchema;

    public XMLValidator(String xmlContent, String xsdSchema) {
        this.xmlContent = xmlContent;
        this.xsdSchema = xsdSchema;
    }

    public boolean validate() {
        if (xmlContent == null || xsdSchema == null) {
            return false;
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(xsdSchema)));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
            return true;
        } catch (SAXException | IOException e) {
            System.err.println("XML validation against XSD scheme failed: " + e);
            return false;
        }
    }
}
