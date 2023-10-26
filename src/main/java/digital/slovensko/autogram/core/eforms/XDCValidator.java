package digital.slovensko.autogram.core.eforms;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.StringReader;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.stream.StreamSource;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.errors.InvalidXMLException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.XMLValidationException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;

public abstract class XDCValidator {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static boolean isXDCContent(DSSDocument document) {
        try {
            var doc = getXmlFromDocument(document);
            var docString = transformElementToString(doc.getDocumentElement());
            var xdcSchema = EFormUtils.class.getResourceAsStream("xmldatacontainer.xsd");

            return validateXmlContentAgainstXsd(docString, new String(xdcSchema.readAllBytes(), ENCODING));

        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    public static void validateXml(String xsdSchema, DSSDocument xmlDocument, String cannonicalizationMethod, DigestAlgorithm digestAlgorithm)
            throws OriginalDocumentNotFoundException, InvalidXMLException, XMLValidationException {
        if (xmlDocument == null)
            throw new XMLValidationException("XML Datacontainer validation failed", "Unable to parse document");

        var xml = EFormUtils.getXmlFromDocument(xmlDocument);
        if (xml == null)
            throw new XMLValidationException("XML Datacontainer validation failed", "Unable to process document");

        if (AutogramMimeType.isXDC(xmlDocument.getMimeType())) {
            if (!XDCValidator.isXDCContent(xmlDocument))
                throw new XMLValidationException("XML Datacontainer validation failed",
                        "Provided XML document is not a valid XML Datacontainer validated by it's XSD schema");

            if (!XDCValidator.validateXsdDigest(xsdSchema, xml.getDocumentElement(), cannonicalizationMethod, digestAlgorithm))
                throw new XMLValidationException("XML Datacontainer validation failed", "XSD digest value mismatch");

            if (!XDCValidator.validateXsltDigest(xsdSchema, xml.getDocumentElement(), cannonicalizationMethod, digestAlgorithm))
                throw new XMLValidationException("XML Datacontainer validation failed", "XSLT digest value mismatch");
        }

        var eformContent = EFormUtils.transformElementToString(AutogramMimeType.isXDC(xmlDocument.getMimeType())
                ? EFormUtils.getEformXmlFromXdcDocument(xmlDocument).getDocumentElement()
                : xml.getDocumentElement());

        if (!validateXmlContentAgainstXsd(eformContent, xsdSchema))
            throw new XMLValidationException("XML validation failed", "XML validation against XSD failed");
    }

    public static boolean validateXmlContentAgainstXsd(String xmlContent, String xsdSchema) {
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

    public static boolean validateXsdDigest(String content, Element document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm) throws InvalidXMLException {
        return validateDigest(content, document, "xdc:UsedXSDReference", canonicalizationMethod, digestAlgorithm);
    }

    public static boolean validateXsltDigest(String content, Element document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm) throws InvalidXMLException {
        return validateDigest(content, document, "xdc:UsedPresentationSchemaReference", canonicalizationMethod,
                digestAlgorithm);
    }

    private static boolean validateDigest(String content, Element document, String fieldWithDigest,
            String canonicalizationMethod, DigestAlgorithm digestAlgorithm) throws InvalidXMLException {
        var contentBytes = content.getBytes(ENCODING);
        var contentHash = computeDigest(contentBytes, canonicalizationMethod, digestAlgorithm, ENCODING);
        var digestValue = getDigestValueFromElement(document, fieldWithDigest);

        return contentHash.equals(digestValue);
    }
}
