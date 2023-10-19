package digital.slovensko.autogram.core.eforms;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.InvalidXMLException;
import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;

public class XDCValidator {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final String xsdSchema;
    private final String xsltSchema;
    private final String canonicalizationMethod;
    private final DigestAlgorithm digestAlgorithm;

    private Document document;


    public static XDCValidator buildFromSigningParametersAndDocument(SigningParameters sp, DSSDocument document) throws InvalidXMLException {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

            return new XDCValidator(sp.getSchema(),
                    sp.getTransformation(),
                    sp.getPropertiesCanonicalization(),
                    sp.getDigestAlgorithm(),
                    builderFactory.newDocumentBuilder().parse(new InputSource(document.openStream())));
        } catch (Exception e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Unable to process document");
        }
    }

    private XDCValidator(String xsdSchema, String xsltSchema, String canonicalizationMethod,
                          DigestAlgorithm digestAlgorithm, Document document) {
        this.xsdSchema = xsdSchema;
        this.xsltSchema = xsltSchema;
        this.canonicalizationMethod = canonicalizationMethod;
        this.digestAlgorithm = digestAlgorithm;
        this.document = document;
    }


    public boolean validateXsdDigest() throws InvalidXMLException {
        try {
            String xsdSchemaHash = computeDigest(xsdSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING);
            String xsdDigestValue = getDigestValueFromElement(document.getDocumentElement(), "UsedXSDReference");
            return xsdSchemaHash.equals(xsdDigestValue);
        } catch (Exception e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Invalid XSD");
        }
    }

    public boolean validateXsltDigest() throws InvalidXMLException {
        try {
            String xsltSchemaHash = computeDigest(xsltSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING);
            String xsltDigestValue = getDigestValueFromElement(document.getDocumentElement(), "UsedPresentationSchemaReference");
            return xsltSchemaHash.equals(xsltDigestValue);
        } catch (Exception e) {
            throw new InvalidXMLException("XML Datacontainer validation failed", "Invalid XSLT");
        }
    }
}
