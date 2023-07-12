package digital.slovensko.autogram;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.XDCTransformer;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XDCTransformerTests {
    @Test
    void testTransformsPlainHtmlWithoutAddingNamespaces() throws IOException {
        var transformation = new String(this.getClass().getResourceAsStream("abc.xslt").readAllBytes());

        var document = new InMemoryDocument(this.getClass().getResourceAsStream("abc.xml"));

        var params = new SigningParameters(
            SignatureLevel.XAdES_BASELINE_B,
            ASiCContainerType.ASiC_E,
            "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
            SignaturePackaging.ENVELOPING,
            DigestAlgorithm.SHA256,
            false,
            CanonicalizationMethod.INCLUSIVE,
            CanonicalizationMethod.INCLUSIVE,
            CanonicalizationMethod.INCLUSIVE,
            null,
            transformation,
            "id1/asa",
            false, 800);

        var out = XDCTransformer.buildFromSigningParameters(params).transform(document);
        var transformed = new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(-1, transformed.lastIndexOf(":p>"));
    }

    @Test
    void testBuildXDCTransformerFromInvalidDocument() {
        var content = "";
        var document =  new InMemoryDocument(content.getBytes(), null);

        var signingParameters = new SigningParameters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                0
        );

        Assertions.assertThrows(RequestValidationException.class, () -> XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document));
    }

    @Test
    void testGetDigestValueElementNotFound() {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new SigningParameters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                schema,
                null,
                null,
                false,
                0
        );

        XDCTransformer xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document);

        Assertions.assertThrows(RequestValidationException.class, xdcTransformer::validateXsdDigest);
    }

    @Test
    void testGetDigestValueAttributesOfElementNotFound() {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference>http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new SigningParameters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                schema,
                null,
                null,
                false,
                0
        );

        XDCTransformer xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document);

        Assertions.assertThrows(RequestValidationException.class, xdcTransformer::validateXsdDigest);
    }

    @Test
    void testGetDigestValueNotFound() {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new SigningParameters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                schema,
                null,
                null,
                false,
                0
        );

        XDCTransformer xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document);

        Assertions.assertThrows(RequestValidationException.class, xdcTransformer::validateXsdDigest);
    }

    @Test
    void testGetContentXDCXmlDataNotFound() {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);

        var signingParameters = new SigningParameters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                0
        );

        XDCTransformer xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document);

        Assertions.assertThrows(RequestValidationException.class, xdcTransformer::getContentFromXdc);
    }

    @Test
    void testGetContentFromXDCInvalidContent() {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData></xdc:XMLData></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);

        var signingParameters = new SigningParameters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                0
        );

        XDCTransformer xdcTransformer = XDCTransformer.buildFromSigningParametersAndDocument(signingParameters, document);

        Assertions.assertThrows(RequestValidationException.class, xdcTransformer::getContentFromXdc);
    }
}
