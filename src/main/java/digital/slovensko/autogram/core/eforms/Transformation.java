package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.xml.crypto.dsig.CanonicalizationMethod;

public class Transformation {
    public Transformation() {
    }

    public static String extractTransformationOutputMimeTypeString(String transformation) throws TransformationParsingErrorException {
        if (transformation == null)
            return "TXT";

        var method = "";
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            var document = builderFactory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(transformation)));
            var elem = document.getDocumentElement();
            var outputElements = elem.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "output");
            if (outputElements.getLength() == 0)
                throw new TransformationParsingErrorException("Failed to parse transformation. Missing output element");

            method = outputElements.item(0).getAttributes().getNamedItem("method").getNodeValue();

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TransformationParsingErrorException("Failed to parse transformation");
        }

        if (method.equals("html"))
            return "HTML";

        if (method.equals("text"))
            return "TXT";

        return null;
    }

    public static String findTransformation(String uri, String xdcXsltDigest) {
        var parts = uri.split("/");
        var lastPart = parts[parts.length - 1];
        var secondLastPart = parts[parts.length - 2];
        var directory = secondLastPart + "/" + lastPart;
        var metaFile = DocumentVisualizationBuilder.class.getResourceAsStream(directory + "/META-INF/manifest.xml");

        var builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        try {
            var inputSource = new InputSource(metaFile);
            inputSource.setEncoding(StandardCharsets.UTF_8.displayName());
            var parsedDocument = builderFactory.newDocumentBuilder().parse(inputSource);
            var xml = parsedDocument.getDocumentElement();

            String xslt = null;
            var entries = xml.getElementsByTagName("manifest:file-entry");

            for (int i = 0; i < entries.getLength(); i++) {
                var entry = entries.item(i);
                var mediaDestinationNode = entry.getAttributes().getNamedItem("media-destination");
                if (mediaDestinationNode == null)
                    continue;

                var mediaDestination = mediaDestinationNode.getNodeValue();
                var fullPath = entry.getAttributes().getNamedItem("full-path");
                if (!mediaDestination.equals("sign") || fullPath == null)
                    continue;

                xslt = fullPath.getNodeValue().replace("\\", "/");
                var transformation = DocumentVisualizationBuilder.class.getResourceAsStream(directory + "/" + xslt).readAllBytes();

                var canonicalizedData = DSSXMLUtils.canonicalize(CanonicalizationMethod.INCLUSIVE, transformation);
                var digest = DSSUtils.digest(DigestAlgorithm.SHA256, canonicalizedData);
                var asBase64 = Base64.getEncoder().encode(digest);

                var xsltDigest = new String(asBase64, StandardCharsets.UTF_8);

                if (xsltDigest.equals(xdcXsltDigest))
                    ;
                else
                    System.out.println("XSLT digest mismatch: " + xsltDigest + " != " + xdcXsltDigest);


                System.out.println("Content: " + new String(transformation, StandardCharsets.UTF_8));
                return new String(transformation, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

}
