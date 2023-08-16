package digital.slovensko.autogram.server.dto;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import digital.slovensko.autogram.core.AsicContainer;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import eu.europa.esig.dss.model.DSSDocument;
import org.xml.sax.InputSource;
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

    public SigningParameters getParameters() throws RequestValidationException, MalformedBodyException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getMimeType());

        var signingParameters = parameters.getSigningParameters(isBase64());
        if (isAsice(getMimetype()) || isXML(getMimetype()) || isXDC(getMimetype()))
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
            String xmlContent = null;
            if (isAsice(getMimetype())) {
               xmlContent = getXmlContentFromAsice(signingParameters);
               if (xmlContent == null) {
                   return;
               }
            } else if (isXDC(getMimetype())) {
               xmlContent = getXmlContentFromXdc(signingParameters, getDocument());
            }  else {
               xmlContent = new String(decodeDocumentContent());
            }

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

    private String getXmlContentFromAsice(SigningParameters signingParameters) throws XMLValidationException, InvalidXMLException {
        AsicContainer asicContainer = new AsicContainer(getDocument());
        DSSDocument originalDocument = asicContainer.getOriginalDocument();
        if (!isXDC(originalDocument.getMimeType()) && !isXML(originalDocument.getMimeType())) {
            return null;
        }
        return isXDC(originalDocument.getMimeType()) ? getXmlContentFromXdc(signingParameters, originalDocument) :
                getXmlContentFromOriginalDocument(originalDocument);
    }

    private String getXmlContentFromOriginalDocument(DSSDocument originalDocument) throws InvalidXMLException {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            org.w3c.dom.Document document = builderFactory.newDocumentBuilder().parse(new InputSource(originalDocument.openStream()));
            var xml = document.getDocumentElement();
            return XDCTransformer.transformElementToString(xml);
        } catch (Exception e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Unable to process document");
        }
    }

    private String getXmlContentFromXdc(SigningParameters signingParameters, DSSDocument document) throws InvalidXMLException, XMLValidationException {
        var xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document);
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
