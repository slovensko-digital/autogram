package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.util.XMLUtils;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class XDCBuilder {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static DSSDocument transform(SigningParameters params, String filename, Document parsedDocument) {
        var identifier = params.getIdentifier();
        var lastSlashIndex = identifier.lastIndexOf("/");
        if (lastSlashIndex == -1)
            throw new RuntimeException("Identifier contains no slash: " + identifier);

        var identifierVersion = identifier.substring(lastSlashIndex + 1);
        try {
            var usedSchemasReferenced = createUsedSchemasReferenced(parsedDocument, params.getSchema(),
                    params.getTransformation(), params.getPropertiesCanonicalization(), params.getDigestAlgorithm(),
                    params.getXsdIdentifier(), params.getXsltParams());

            var transformedDocument = transformDocument(
                    parsedDocument, params.getContainerXmlns(), identifier, identifierVersion, usedSchemasReferenced);

            var content = getDocumentContent(transformedDocument).getBytes(ENCODING);

            return new InMemoryDocument(content, filename);

        } catch (TransformationException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformationException("Nastala chyba počas transformácie dokumentu",
                    "Nastala chyba počas transformácie dokumentu", e);
        }
    }

    private static Document transformDocument(Document document, String containerXmlns, String identifierUri,
            String identifierVersion, Element usedSchemasReferenced) {
        var root = document.getDocumentElement();
        var xmlDataContainer = createXMLDataContainer(document, containerXmlns);
        var xmlData = createXMLData(document, identifierUri, identifierVersion);


        xmlDataContainer.appendChild(xmlData);
        xmlData.appendChild(root);
        if (usedSchemasReferenced != null)
            xmlDataContainer.appendChild(usedSchemasReferenced);

        document.appendChild(xmlDataContainer);
        return document;
    }

    private static String getDocumentContent(Document document) throws TransformationException, TransformerException {
        document.setXmlStandalone(true);
        var xmlSource = new DOMSource(document);
        var outputTarget = new StreamResult(new StringWriter());
        XMLUtils.getSecureTransformerFactory().newTransformer().transform(xmlSource, outputTarget);

        return outputTarget.getWriter().toString();
    }

    private static Element createXMLDataContainer(Document document, String containerXmlns) {
        var element = document.createElement("xdc:XMLDataContainer");
        if (containerXmlns != null)
            element.setAttribute("xmlns:xdc", containerXmlns);

        return element;
    }

    private static Element createXMLData(Document document, String identifierUri, String identifierVersion) {
        var element = document.createElement("xdc:XMLData");
        element.setAttribute("ContentType", "application/xml; charset=UTF-8");
        element.setAttribute("Identifier", identifierUri);
        element.setAttribute("Version", identifierVersion);

        return element;
    }

    private static Element createUsedSchemasReferenced(Document document, String xsdSchema, String xsltSchema,
            String cannonicalizationMethod, DigestAlgorithm digestAlgorithm, String xsdIdentifier, XsltParams xsltParams) {
        var element = document.createElement("xdc:UsedSchemasReferenced");

        if (xsdSchema != null)
            element.appendChild(createUsedXSDReference(document, cannonicalizationMethod, digestAlgorithm, xsdSchema,
                    xsdIdentifier));

        if (xsltSchema != null)
            element.appendChild(createUsedPresentationSchemaReference(document, cannonicalizationMethod,
                    digestAlgorithm, xsltSchema, xsltParams));

        return element;
    }

    private static Element createUsedXSDReference(Document document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm, String xsdSchema, String xsdIdentifier) {
        var element = document.createElement("xdc:UsedXSDReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue",
                computeDigest(xsdSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING));
        element.setTextContent(xsdIdentifier);

        return element;
    }

    private static Element createUsedPresentationSchemaReference(Document document, String canonicalizationMethod,
            DigestAlgorithm digestAlgorithm, String xsltSchema, XsltParams xsltParams) {
        var element = document.createElement("xdc:UsedPresentationSchemaReference");
        element.setAttribute("TransformAlgorithm", canonicalizationMethod);
        element.setAttribute("DigestMethod", toNamespacedString(digestAlgorithm));
        element.setAttribute("DigestValue",
                computeDigest(xsltSchema.getBytes(ENCODING), canonicalizationMethod, digestAlgorithm, ENCODING));
        element.setAttribute("ContentType", xsltParams.mediaType());
        element.setAttribute("MediaDestinationTypeDescription", xsltParams.destinationType());
        element.setAttribute("Language", xsltParams.language());
        if (xsltParams.target() != null)
            element.setAttribute("TargetEnvironment", xsltParams.target());

        element.setTextContent(xsltParams.identifier());

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) {
        return "urn:oid:" + digestAlgorithm.getOid();
    }
}
