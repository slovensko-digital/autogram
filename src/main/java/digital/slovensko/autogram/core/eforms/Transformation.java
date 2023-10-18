package digital.slovensko.autogram.core.eforms;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.xades.DSSXMLUtils;

public abstract class Transformation {
    private static final String SOURCE_URL = "https://test-autogram-eforms-marek.s3.eu-central-1.amazonaws.com/v1/eforms/";

    public static String extractTransformationOutputMimeTypeString(String transformation)
            throws TransformationParsingErrorException {
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

    public static String findTransformation(DSSDocument documentToDisplay) throws AutogramException {
        byte[] xsltString = null;
        var xdc = getFormXdc(documentToDisplay);
        var formUri = getFormUri(xdc);

        try {
            xsltString = downloadTransformation(formUri);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // var xsltDigest = computeDigest(xsltString, CanonicalizationMethod.INCLUSIVE);
        // var xdcXsltDigest = getXdcXsltDigest(xdc);
        // if (!xsltDigest.equals(xdcXsltDigest))
        // throw new AutogramException("XSLT digest mismatch", "XSLT digest mismatch",
        // "XSLT digest mismatch: " + xsltDigest + " != " + xdcXsltDigest);

        return new String(xsltString, StandardCharsets.UTF_8);
    }

    private static String computeDigest(byte[] data, String canonicalizationMethod) {
        var canonicalizedData = DSSXMLUtils.canonicalize(canonicalizationMethod, data);
        var digest = DSSUtils.digest(DigestAlgorithm.SHA256, canonicalizedData);
        var asBase64 = Base64.getEncoder().encode(digest);

        return new String(asBase64, StandardCharsets.UTF_8);
    }

    private static Element getFormXdc(DSSDocument documentToDisplay) {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

            var is = documentToDisplay.openStream();
            var inputSource = new InputSource(is);
            inputSource.setEncoding(StandardCharsets.UTF_8.displayName());
            var parsedDocument = builderFactory.newDocumentBuilder().parse(inputSource);
            return parsedDocument.getDocumentElement();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getXdcXsltDigest(Element xdc) {
        return xdc.getElementsByTagNameNS(
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "UsedXSDReference").item(0).getAttributes()
                .getNamedItem("DigestValue").getNodeValue();

    }

    private static String getFormUri(Element xdc) {
        var xmlData = xdc.getElementsByTagNameNS(
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData").item(0);

        if (xmlData == null)
            return null;

        return xmlData.getAttributes().getNamedItem("Identifier").getNodeValue();
    }

    private static byte[] downloadTransformation(String uri) throws IOException {
        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(21600000);
        offlineFileLoader.setDataLoader(new CommonsDataLoader());

        var parts = uri.split("/");
        var formVersion = parts[parts.length - 1];
        var formIdentifier = parts[parts.length - 2];
        var formDirectory = formIdentifier + "/" + formVersion;

        var xsltDoc = offlineFileLoader.getDocument(SOURCE_URL + formDirectory + "/form.sb.xslt");
        if (xsltDoc == null)
            return null;

        return xsltDoc.openStream().readAllBytes();
    }
}
