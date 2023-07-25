package digital.slovensko.autogram.util;

import digital.slovensko.autogram.core.AutogramMimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DSSDocumentUtils {

    public static boolean isPlainText(DSSDocument document) {
        return document.getMimeType().equals(MimeTypeEnum.TEXT);
    }

    public static boolean isPdf(DSSDocument document) {
        return document.getMimeType().equals(MimeTypeEnum.PDF);
    }

    public static boolean isImage(DSSDocument document) {
        return document.getMimeType().equals(MimeTypeEnum.JPEG) || document.getMimeType().equals(MimeTypeEnum.PNG);
    }

    public static boolean isXDC(DSSDocument document) {
        return document.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER);
    }

    public static boolean isXML(DSSDocument document) {
        return document.getMimeType().equals(MimeTypeEnum.XML);
    }

    public static boolean isAsice(DSSDocument document) {
        return document.getMimeType().equals(MimeTypeEnum.ASICE);
    }

    public static String getDocumentAsPlainText(DSSDocument document, String transformation) {
        if (document.getMimeType().equals(MimeTypeEnum.TEXT)) {
            try {
                return new String(document.openStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return transform(document, transformation);
        }
    }

    public static String getDocumentAsBase64Encoded(DSSDocument document) {
        try {
            return new String(Base64.getEncoder().encode(document.openStream().readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String transform(DSSDocument commonDocument, String transformation) {
        // TODO probably move this logic into signing job creation
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(commonDocument.openStream()));

            var xmlSource = new DOMSource(document);
            if (isXDC(commonDocument))
                xmlSource = extractFromXDC(document, builderFactory);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = TransformerFactory.newInstance().newTransformer(
                    new StreamSource(new ByteArrayInputStream(transformation.getBytes())));
            transformer.transform(xmlSource, outputTarget);

            return outputTarget.getWriter().toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // TODO
        }
    }

    private static DOMSource extractFromXDC(Document document, DocumentBuilderFactory builderFactory)
            throws ParserConfigurationException {
        var xdc = document.getDocumentElement();

        var xmlData = xdc.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData")
                .item(0);

        if (xmlData == null)
            throw new RuntimeException("XMLData not found in XDC"); // TODO catch somewhere

        document = builderFactory.newDocumentBuilder().newDocument();
        var node = document.importNode(xmlData.getFirstChild(), true);
        document.appendChild(node);

        return new DOMSource(document);
    }
}
