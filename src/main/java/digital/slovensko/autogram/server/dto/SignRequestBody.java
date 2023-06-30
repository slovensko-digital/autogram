package digital.slovensko.autogram.server.dto;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.XDCTransformer;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class SignRequestBody {
    private final Document document;
    private final ServerSigningParameters parameters;
    private final String payloadMimeType;

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType) {
        this.document = document;
        this.parameters = parameters;
        this.payloadMimeType = payloadMimeType;
    }

    public InMemoryDocument getDocument() throws RequestValidationException {
        if (payloadMimeType == null)
            throw new RequestValidationException("PayloadMimeType is required", "");

        if (document == null)
            throw new RequestValidationException("Document is required", "");

        if (document.getContent() == null)
            throw new RequestValidationException("Document.Content is required", "");

        byte[] content;
        if (isBase64()) {
            content = Base64.getDecoder().decode(document.getContent());
        } else {
            content = document.getContent().getBytes();
        }

        var filename = document.getFilename();
        var mimetype = AutogramMimeType.fromMimeTypeString(payloadMimeType.split(";")[0]);

        return new InMemoryDocument(content, filename, mimetype);
    }

    public SigningParameters getParameters() throws RequestValidationException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getMimeType());

        SigningParameters signingParameters = parameters.getSigningParameters(isBase64());
        
        validateXml(signingParameters);

        return signingParameters;
    }

    private boolean isBase64() {
        return payloadMimeType.contains("base64");
    }

    private void validateXml(SigningParameters signingParameters) {
        String xsdSchema = signingParameters.getSchema();
        String xmlContent;

        if (isXdc()) {
            if (!validateXsdAndXsltHashes(signingParameters)) {
                throw new RequestValidationException("XML validation failed", "Invalid xsd scheme or xslt transformation");
            }

            xmlContent = getContentFromXdc();
            if (xmlContent == null) {
                throw new RequestValidationException("XML validation failed", "Unable to get content from xdc container");
            }
        } else {
            xmlContent = getDecodedContent();
        }

        if (!validateXmlContentAgainstXsd(xmlContent, xsdSchema)) {
            throw new RequestValidationException("XML validation failed", "XML validation against XSD failed");
        }
    }

    private boolean isXdc() {
        return getDocument().getMimeType().equals(AutogramMimeType.XML_DATACONTAINER);
    }

    private boolean validateXsdAndXsltHashes(SigningParameters sp) {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            XDCTransformer xdcTransformer = new XDCTransformer(
                    sp.getSchema(),
                    sp.getTransformation(),
                    sp.getPropertiesCanonicalization(),
                    sp.getDigestAlgorithm(),
                    builderFactory.newDocumentBuilder().parse(new InputSource(getDocument().openStream()))
            );

            return xdcTransformer.validateXsd() && xdcTransformer.validateXslt();
        } catch (Exception e) {
            return false;
        }
    }

    private String getContentFromXdc() {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(getDocument().openStream()));
            var element = XDCTransformer.extractFromXDC(document, builderFactory);
            return transformElementToString(element);
        } catch (Exception e) {
            return null;
        }
    }

    private static String transformElementToString(DOMSource element) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(element, new StreamResult(writer));
        return writer.toString();
    }

    private String getDecodedContent() {
        return isBase64() ? new String(Base64.getDecoder().decode(document.getContent())) : document.getContent();
    }

    private boolean validateXmlContentAgainstXsd(String xmlContent, String xsdSchema) {
        if (xsdSchema == null) {
            return true;
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(xsdSchema)));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
            return true;
        } catch (SAXException | IOException e) {
            return false;
        }
    }
}
