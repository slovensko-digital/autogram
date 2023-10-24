package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import eu.europa.esig.dss.model.DSSDocument;

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
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.*;
import static digital.slovensko.autogram.core.eforms.EFormUtils.*;

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

        var signingParameters = parameters.getSigningParameters(isBase64(), getDocument());
        var parsedPaylodMimeType = getMimetype();
        if (isAsice(parsedPaylodMimeType) || isXML(parsedPaylodMimeType) || isXDC(parsedPaylodMimeType))
        validateXml(signingParameters);

        signingParameters.extractTransformationOutputMimeTypeString();

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

            var xml = EFormUtils.getXmlFromDocument(xmlDocument);
            if (xml == null)
                throw new XMLValidationException("XML Datacontainer validation failed", "Unable to process document");

            var xmlContent = EFormUtils.transformElementToString(xml.getDocumentElement());
            if (isXDC(xmlDocument.getMimeType())) {
                validateXdcDigests(signingParameters, xmlContent);

                if (!isXDCContent(xmlDocument))
                    throw new XMLValidationException("XML Datacontainer validation failed",
                            "Provided XML document is not a valid XML Datacontainer validated by it's XSD schema");
            }

            var eformContent = EFormUtils.transformElementToString(isXDC(xmlDocument.getMimeType())
                    ? EFormUtils.getEformXmlFromXdcDocument(xmlDocument).getDocumentElement()
                    : xml.getDocumentElement());
            if (!EFormUtils.validateXmlContentAgainstXsd(eformContent, xsdSchema))
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
            return getXdcDocumentFromAsice(signingParameters);
        } else {
            return getDocument();
        }
    }

    private DSSDocument getXdcDocumentFromAsice(SigningParameters signingParameters)
            throws XMLValidationException, InvalidXMLException {
        var originalDocument = AsicContainerUtils.getOriginalDocument(getDocument());
        if (!isXDC(originalDocument.getMimeType())) {
            return null;
        }
        return originalDocument;
    }

    private void validateXdcDigests(SigningParameters signingParameters, String content)
            throws InvalidXMLException, XMLValidationException {
        var xdcValidator = XDCValidator.buildFromSigningParametersAndDocument(signingParameters,
                new InMemoryDocument(content.getBytes()));
        if (signingParameters.getSchema() != null && !xdcValidator.validateXsdDigest())
            throw new XMLValidationException("XML Datacontainer validation failed", "XSD scheme digest mismatch");

        if (signingParameters.getTransformation() != null && !xdcValidator.validateXsltDigest())
            throw new XMLValidationException("XML Datacontainer validation failed",
                    "XSLT transformation digest mismatch");
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
