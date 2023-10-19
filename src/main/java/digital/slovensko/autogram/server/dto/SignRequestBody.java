package digital.slovensko.autogram.server.dto;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import eu.europa.esig.dss.model.DSSDocument;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.XDCValidator;
import digital.slovensko.autogram.core.errors.InvalidXMLException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

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

    public SigningParameters getParameters()
            throws RequestValidationException, MalformedBodyException, TransformationParsingErrorException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getMimeType());

        var signingParameters = parameters.getSigningParameters(isBase64());
        signingParameters.extractTransformationOutputMimeTypeString();
        var parsedPaylodMimeType = getMimetype();
        if (isAsice(parsedPaylodMimeType) || isXML(parsedPaylodMimeType) || isXDC(parsedPaylodMimeType))
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

    private void validateXml(SigningParameters signingParameters)
            throws RequestValidationException, MalformedBodyException {
        var xsdSchema = signingParameters.getSchema();

        try {
            var xmlDocument = getXmlDocument(signingParameters);
            if (xmlDocument == null)
                return;

            var xmlContent = EFormUtils.transformElementToString(EFormUtils.getXmlFromDocument(xmlDocument));
            if (isXDC(xmlDocument.getMimeType()))
                validateXdcDigests(signingParameters, xmlContent);

            if (!validateXmlContentAgainstXsd(xmlContent, xsdSchema))
                throw new XMLValidationException("XML validation failed", "XML validation against XSD failed");

        } catch (OriginalDocumentNotFoundException e) {
            throw new MalformedBodyException(e.getMessage(), e.getDescription());
        } catch (InvalidXMLException e) {
            throw new MalformedBodyException(e.getMessage(), e.getDescription());
        } catch (XMLValidationException e) {
            throw new RequestValidationException(e.getMessage(), e.getDescription());
        }
    }

    private DSSDocument getXmlDocument(SigningParameters signingParameters)
            throws XMLValidationException, InvalidXMLException {
        if (isAsice(getMimetype())) {
            return getDocumentFromAsice(signingParameters);
        } else if (isXDC(getMimetype())) {
            return getXmlDocumentFromXdc(signingParameters, getDocument());
        } else {
            return getDocument();
        }
    }

    private DSSDocument getDocumentFromAsice(SigningParameters signingParameters)
            throws XMLValidationException, InvalidXMLException {
        var originalDocument = AsicContainerUtils.getOriginalDocument(getDocument());
        if (!isXDC(originalDocument.getMimeType())) {
            return null;
        }
        return originalDocument;
    }

    private void validateXdcDigests(SigningParameters signingParameters, String content)
            throws InvalidXMLException, XMLValidationException {
        var xdcValidator = XDCValidator.buildFromSigningParametersAndDocument(signingParameters, new InMemoryDocument(content.getBytes()));
        if (signingParameters.getSchema() != null && !xdcValidator.validateXsdDigest())
            throw new XMLValidationException("XML Datacontainer validation failed", "XSD scheme digest mismatch");

        if (signingParameters.getTransformation() != null && !xdcValidator.validateXsltDigest())
            throw new XMLValidationException("XML Datacontainer validation failed",
                    "XSLT transformation digest mismatch");
    }

    private DSSDocument getXmlDocumentFromXdc(SigningParameters signingParameters, DSSDocument document)
            throws InvalidXMLException {
        var doc = EFormUtils.getXmlFromDocument(document);
        var data = EFormUtils.transformElementToString(doc).getBytes();
        return new InMemoryDocument(data, document.getName(), MimeTypeEnum.XML);
    }

    private boolean validateXmlContentAgainstXsd(String xmlContent, String xsdSchema) {
        if (xsdSchema == null)
            return true;

        try {
            var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
