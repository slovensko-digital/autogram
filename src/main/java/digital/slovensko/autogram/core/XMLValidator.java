package digital.slovensko.autogram.core;

import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

public class XMLValidator {

    private String xmlContent;

    private String xsdSchema;

    public XMLValidator(SignRequestBody body) {
        this.xmlContent = body.isBase64() ? new String(Base64.getDecoder().decode(body.getContent())) : body.getContent();
        this.xsdSchema = body.getParameters().getSchema();
    }

    public void validate() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(xsdSchema)));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
        } catch (SAXException | IOException e) {
            throw new RequestValidationException("XML validation against XSD scheme failed", "");
        }
    }
}
