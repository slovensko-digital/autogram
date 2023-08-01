package digital.slovensko.autogram.server.dto;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.XDCTransformer;
import digital.slovensko.autogram.core.errors.InvalidXMLException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;

public class SignRequestBody {
    private final Document document;
    private final ServerSigningParameters parameters;
    private final String payloadMimeType;
    private final String batchId;

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType) {
        this(document, parameters, payloadMimeType, null);
    }

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType,
            String batchId) {
        this.document = document;
        this.parameters = parameters;
        this.payloadMimeType = payloadMimeType;
        this.batchId = batchId;
    }

    public InMemoryDocument getDocument() throws RequestValidationException {
        if (payloadMimeType == null)
            throw new RequestValidationException("PayloadMimeType is required", "");

        if (document == null)
            throw new RequestValidationException("Document is required", "");

        if (document.getContent() == null)
            throw new RequestValidationException("Document.Content is required", "");

        var content = decodeDocumentContent();
        var filename = document.getFilename();

        return new InMemoryDocument(content, filename, getMimetype());
    }

    public SigningParameters getParameters() throws RequestValidationException, MalformedBodyException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getMimeType());

        var signingParameters = parameters.getSigningParameters(isBase64());
        if (isXML(getMimetype()) || isXDC(getMimetype()))
            validateXml(signingParameters);

        return signingParameters;
    }

    private MimeType getMimetype() {
        return AutogramMimeType.fromMimeTypeString(payloadMimeType.split(";")[0]);
    }

    public String getBatchId() {
        return batchId;
    }

    private boolean isBase64() {
        return payloadMimeType.contains("base64");
    }

    private void validateXml(SigningParameters signingParameters) throws RequestValidationException, MalformedBodyException {
        var xsdSchema = signingParameters.getSchema();

        try {
            var xmlContent = isXDC(getMimetype()) ? getXmlContentFromXdc(signingParameters)
                    : new String(decodeDocumentContent());

            if (!validateXmlContentAgainstXsd(xmlContent, xsdSchema))
                throw new XMLValidationException("XML validation failed", "XML validation against XSD failed");

        } catch (InvalidXMLException e) {
            throw new MalformedBodyException(e.getMessage(), e.getDescription());

        } catch (XMLValidationException e) {
            throw new RequestValidationException(e.getMessage(), e.getDescription());
        }
    }

    private String getXmlContentFromXdc(SigningParameters signingParameters) throws InvalidXMLException, XMLValidationException {
        var xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, getDocument());
        if (signingParameters.getSchema() != null && !xdcTransformer.validateXsdDigest())
            throw new XMLValidationException("XML Datacontainer validation failed", "XSD scheme digest mismatch");

        if (signingParameters.getTransformation() != null && !xdcTransformer.validateXsltDigest())
            throw new XMLValidationException("XML Datacontainer validation failed",
                    "XSLT transformation digest mismatch");

        return xdcTransformer.getContentFromXdc();
    }

    private boolean validateXmlContentAgainstXsd(String xmlContent, String xsdSchema) {
        if (xsdSchema == null)
            return true;

        try {
            var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            var schema = factory.newSchema(new StreamSource(new StringReader(xsdSchema)));
            var validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));

            return true;

        } catch (SAXException | IOException | IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] decodeDocumentContent() throws MalformedBodyException {
        try {
            if (isBase64())
                return Base64.getDecoder().decode(document.getContent());

            return document.getContent().getBytes();

        } catch (IllegalArgumentException e) {
            throw new MalformedBodyException("Base64 decoding failed", "Invalid document content");
        }
    }
}
