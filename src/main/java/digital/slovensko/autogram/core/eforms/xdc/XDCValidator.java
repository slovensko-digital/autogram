package digital.slovensko.autogram.core.eforms.xdc;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.util.XMLUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static digital.slovensko.autogram.core.eforms.EFormUtils.computeDigest;
import static digital.slovensko.autogram.core.eforms.EFormUtils.getDigestValueFromElement;
import static digital.slovensko.autogram.core.eforms.EFormUtils.transformElementToString;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.DATACONTAINER_XSD_VIOLATION;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.FAILED_TO_LOAD_XML;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.FAILED_TO_LOAD_XML_DATA;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.XSD_DIGEST_MISMATCH;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.XSD_VIOLATION;
import static digital.slovensko.autogram.core.errors.XMLValidationException.Error.XSLT_DIGEST_MISMATCH;

public abstract class XDCValidator {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static boolean isXDCContent(DSSDocument document) {
        try {
            var is = document.openStream();
            var docString = new String(is.readAllBytes(), ENCODING);
            var xdcSchema = EFormUtils.class.getResourceAsStream("xmldatacontainer.xsd");

            return validateXmlContentAgainstXsd(docString, new String(xdcSchema.readAllBytes(), ENCODING));

        } catch (IOException | NullPointerException | XMLValidationException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void validateXml(String xsd, String xslt, DSSDocument xmlDocument, String cannonicalizationMethod, DigestAlgorithm digestAlgorithm, boolean embedUsedSchemas)
            throws OriginalDocumentNotFoundException, XMLValidationException, XMLValidationException {
        if (xmlDocument == null)
            throw new XMLValidationException(FAILED_TO_LOAD_XML);

        var xml = EFormUtils.getXmlFromDocument(xmlDocument);
        if (xml == null)
            throw new XMLValidationException(FAILED_TO_LOAD_XML_DATA);

        if (AutogramMimeType.isXDC(xmlDocument.getMimeType())) {
            if (!isXDCContent(xmlDocument))
                throw new XMLValidationException(DATACONTAINER_XSD_VIOLATION);

            if (!embedUsedSchemas && xsd != null && !validateXsdDigest(xsd, xml.getDocumentElement(), cannonicalizationMethod, digestAlgorithm))
                throw new XMLValidationException(XSD_DIGEST_MISMATCH);

            if (!embedUsedSchemas && xslt != null && !validateXsltDigest(xslt, xml.getDocumentElement(), cannonicalizationMethod, digestAlgorithm))
                throw new XMLValidationException(XSLT_DIGEST_MISMATCH);
        }

        var eformContent = transformElementToString(
                AutogramMimeType.isXDC(xmlDocument.getMimeType())
                    ? EFormUtils.getEformXmlFromXdcDocument(xmlDocument).getDocumentElement()
                    : xml.getDocumentElement()
        );

        if (xsd != null && !validateXmlContentAgainstXsd(eformContent, xsd))
            throw new XMLValidationException(XSD_VIOLATION);
    }

    public static boolean validateXmlContentAgainstXsd(String xmlContent, String xsdSchema) {
        if (xsdSchema == null)
            return true;

        try {
            var schema = XMLUtils.getSecureSchemaFactory().newSchema(new StreamSource(new StringReader(xsdSchema)));
            var validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));

            return true;

        } catch (SAXException | IOException | IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean validateXsdDigest(String content, Element document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm) throws XMLValidationException {
        return validateDigest(content, document, "UsedXSDReference", canonicalizationMethod, digestAlgorithm);
    }

    public static boolean validateXsltDigest(String content, Element document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm) throws XMLValidationException {
        return validateDigest(content, document, "UsedPresentationSchemaReference", canonicalizationMethod,
                digestAlgorithm);
    }

    private static boolean validateDigest(String content, Element document, String fieldWithDigest,
            String canonicalizationMethod, DigestAlgorithm digestAlgorithm) throws XMLValidationException {
        var contentBytes = content.getBytes(ENCODING);
        var contentHash = computeDigest(contentBytes, canonicalizationMethod, digestAlgorithm, ENCODING);
        var digestValue = getDigestValueFromElement(document, fieldWithDigest);

        return contentHash.equals(digestValue);
    }
}
