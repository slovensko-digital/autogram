package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XMLValidatorTest {
    private XMLValidator xmlValidator;

    @Test
    void validateNullValuesTest() {
        xmlValidator = new XMLValidator(null, null);
        Assertions.assertFalse(xmlValidator.validate());
    }


    @Test
    void validateEmptyValuesTest() {
        xmlValidator = new XMLValidator("", "");
        Assertions.assertFalse(xmlValidator.validate());
    }

    @Test
    void validateInvalidXmlTest() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        String scheme = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>\n";
        xmlValidator = new XMLValidator(xmlContent, scheme);
        Assertions.assertFalse(xmlValidator.validate());
    }

    @Test
    void validateValidXmlTest() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n  <subject>Nové podanie</subject>\n  <text>Podávam toto nové podanie.</text>\n</GeneralAgenda>";
        String scheme = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>\n";
        xmlValidator = new XMLValidator(xmlContent, scheme);
        Assertions.assertTrue(xmlValidator.validate());
    }
}