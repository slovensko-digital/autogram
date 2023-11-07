package digital.slovensko.autogram.core.eforms;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.StringReader;
import java.io.StringWriter;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.util.XMLUtils;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;

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

    public static void validateXml(String xsd, String xslt, DSSDocument xmlDocument, String cannonicalizationMethod, DigestAlgorithm digestAlgorithm)
            throws OriginalDocumentNotFoundException, XMLValidationException, XMLValidationException {
        if (xmlDocument == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dokument");

        var xml = EFormUtils.getXmlFromDocument(xmlDocument);
        if (xml == null)
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dáta z XML Datacontainera");

        if (AutogramMimeType.isXDC(xmlDocument.getMimeType())) {
            if (!isXDCContent(xmlDocument))
                throw new XMLValidationException("Zlyhala validácia XML Datacontainera",
                        "Poskytnutý XML dokument nie je validný XML Datacontainer podľa XSD schémy");

            if (xsd != null && !validateXsdDigest(xsd, xml.getDocumentElement(), cannonicalizationMethod, digestAlgorithm))
                throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "XSD schéma sa nezhoduje s odtlačkom v XML Datacontaineri");

            if (xslt != null && !validateXsltDigest(xslt, xml.getDocumentElement(), cannonicalizationMethod, digestAlgorithm))
                throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "XSLT transformácia sa nezhoduje s odtlačkom v XML Datacontaineri");
        }

        var eformContent = transformElementToString(AutogramMimeType.isXDC(xmlDocument.getMimeType())
                ? EFormUtils.getEformXmlFromXdcDocument(xmlDocument).getDocumentElement()
                : xml.getDocumentElement());

        if (xsd != null && !validateXmlContentAgainstXsd(eformContent, xsd))
            throw new XMLValidationException("Zlyhala validícia XML dokumentu", "XML dokument sa nezhoduje s XSD schémou");
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

    private static String transformElementToString(Element element) {
            try {
                var document = XMLUtils.getSecureDocumentBuilder().newDocument();
                Node node;
            try {
                node = document.importNode(element, true);
            } catch (DOMException e) {
                node = document.importNode(element.getFirstChild(), true);
            }

            document.appendChild(node);

            var transformer = XMLUtils.getSecureTransformerFactory().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            var writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new XMLValidationException("Zlyhala validácia XML Datacontainera", "Nepodarilo sa načítať XML dokument", e);
        }
    }
}
