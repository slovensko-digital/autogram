package digital.slovensko.autogram.core.eforms.xdc;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.util.XMLUtils;

import static digital.slovensko.autogram.core.eforms.EFormUtils.*;
import static digital.slovensko.autogram.util.XMLUtils.getSecureDocumentBuilder;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class XDCBuilder {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static DSSDocument transform(SigningParameters params, String filename, Document document) {
        var identifier = params.getIdentifier();
        var lastSlashIndex = identifier.lastIndexOf("/");
        if (lastSlashIndex == -1)
            throw new TransformationException("Nastala chyba počas transformácie dokumentu", "XDC identifikátor formulára neobsahuje žiadnu lomku: " + identifier);

        var identifierVersion = identifier.substring(lastSlashIndex + 1);
        if (!identifierVersion.matches("[v0-9.]+"))
            identifierVersion = "1.0";

        try {
            var parsedDocument = getSecureDocumentBuilder().newDocument();
            var importedNode = parsedDocument.importNode(document.getDocumentElement(), true);
            parsedDocument.appendChild(importedNode);
            var usedSchemas = params.shouldEmbedSchemas()
                    ? createUsedSchemasEmbedded(parsedDocument, params.getSchema(),
                        params.getTransformation(), params.getXsltParams())
                    : createUsedSchemasReferenced(parsedDocument, params.getSchema(),
                        params.getTransformation(), params.getPropertiesCanonicalization(), params.getDigestAlgorithm(),
                        params.getXsdIdentifier(), params.getXsltParams());

            var transformedDocument = transformDocument(parsedDocument, params.getContainerXmlns(), identifier,
                    identifierVersion, usedSchemas, params.shouldEmbedSchemas());

            var content = getDocumentContent(transformedDocument).getBytes(ENCODING);

            if (!filename.contains("."))
                filename += "." + AutogramMimeType.XML_DATACONTAINER.extension();

            else if (filename.endsWith(".xml"))
                filename = filename.replace(".xml", "." + AutogramMimeType.XML_DATACONTAINER.extension());

            else if (!filename.contains("." + AutogramMimeType.XML_DATACONTAINER.extension()))
                filename += "." + AutogramMimeType.XML_DATACONTAINER.extension();

            return new InMemoryDocument(content, filename, AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET);

        } catch (Exception e) {
            throw new TransformationException("Nastala chyba počas transformácie dokumentu",
                    "Nastala chyba počas transformácie dokumentu", e);
        }
    }

    private static Document transformDocument(Document document, String containerXmlns, String identifierUri,
            String identifierVersion, Element usedSchemas, boolean shouldEmbedSchemas) {
        var root = document.getDocumentElement();
        var xmlDataContainer = createXMLDataContainer(document, containerXmlns);
        var xmlData = createXMLData(document, identifierUri, identifierVersion, shouldEmbedSchemas);


        xmlDataContainer.appendChild(xmlData);
        xmlData.appendChild(root);
        if (usedSchemas != null)
            xmlDataContainer.appendChild(usedSchemas);

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

    private static Element createXMLData(Document document, String identifierUri, String identifierVersion,
            boolean shouldEmbedSchemas) {
        var element = document.createElement("xdc:XMLData");
        element.setAttribute("ContentType", "application/xml; charset=UTF-8");

        if (!shouldEmbedSchemas) {
            element.setAttribute("Identifier", identifierUri);
            element.setAttribute("Version", identifierVersion);
        }

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

    private static Element createUsedSchemasEmbedded(Document document, String xsdSchema, String xsltSchema,
            XsltParams xsltParams) throws ParserConfigurationException, IOException, SAXException {
        var element = document.createElement("xdc:UsedSchemasEmbedded");

        if (xsdSchema != null)
            element.appendChild(createUsedXSDEmbedded(document, xsdSchema));

        if (xsltSchema != null)
            element.appendChild(createUsedPresentationSchemaEmbedded(document, xsltSchema, xsltParams));

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

    private static Element createUsedXSDEmbedded(Document document, String xsdSchema)
            throws ParserConfigurationException, IOException, SAXException {
        var element = document.createElement("xdc:UsedXSDEmbedded");
        var parsedSchema = getSecureDocumentBuilder().parse(new InputSource(new StringReader(xsdSchema)));
        var importedNode = document.importNode(parsedSchema.getFirstChild(), true);
        element.appendChild(importedNode);

        return element;
    }

    private static Element createUsedPresentationSchemaEmbedded(Document document, String xsltSchema, XsltParams xsltParams)
            throws ParserConfigurationException, IOException, SAXException {
        var element = document.createElement("xdc:UsedPresentationSchemaEmbedded");
        element.setAttribute("ContentType", xsltParams.mediaType());
        element.setAttribute("MediaDestinationTypeDescription", xsltParams.destinationType());
        element.setAttribute("Language", xsltParams.language());
        if (xsltParams.target() != null)
            element.setAttribute("TargetEnvironment", xsltParams.target());

        var parsedSchema = getSecureDocumentBuilder().parse(new InputSource(new StringReader(xsltSchema)));
        var importedNode = document.importNode(parsedSchema.getFirstChild(), true);
        element.appendChild(importedNode);

        return element;
    }

    private static String toNamespacedString(DigestAlgorithm digestAlgorithm) {
        return "urn:oid:" + digestAlgorithm.getOid();
    }
}
