package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

class SignRequestBodyTest {

    @Test
    void testValidateXDCWithoutXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCWithValidXmlAgainstXSD() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("def.xml").readAllBytes());
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCWithInvalidXmlAgainstXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCInvalidXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCInvalidXSLT() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var transformation = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  xmlns:egonp=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xsl:output method=\"text\" indent=\"yes\" omit-xml-declaration=\"yes\"/>\n<xsl:strip-space elements=\"*\" />\n<xsl:template match=\"egonp:GeneralAgenda\">\n<xsl:text>Všeobecná agenda</xsl:text>\n<xsl:apply-templates/>\n</xsl:template>\n<xsl:template match=\"egonp:GeneralAgenda/egonp:subject\">\n<xsl:if test=\"./text()\">\n<xsl:text>&#xA;</xsl:text>\n<xsl:text>&#09;</xsl:text><xsl:text>Predmet: </xsl:text><xsl:call-template name=\"string-replace-all\"><xsl:with-param name=\"text\" select=\".\" /><xsl:with-param name=\"replace\" select=\"'&#10;'\" /><xsl:with-param name=\"by\" select=\"'&#13;&#10;&#09;'\" /></xsl:call-template>\n</xsl:if>\n</xsl:template>\n<xsl:template match=\"egonp:GeneralAgenda/egonp:text\">\n<xsl:if test=\"./text()\">\n<xsl:text>&#xA;</xsl:text>\n<xsl:text>&#09;</xsl:text><xsl:text>Text: </xsl:text><xsl:call-template name=\"string-replace-all\"><xsl:with-param name=\"text\" select=\".\" /><xsl:with-param name=\"replace\" select=\"'&#10;'\" /><xsl:with-param name=\"by\" select=\"'&#13;&#10;&#09;'\" /></xsl:call-template>\n</xsl:if>\n</xsl:template>\n<xsl:template name=\"formatToSkDate\">\n<xsl:param name=\"date\" />\n<xsl:variable name=\"dateString\" select=\"string($date)\" />\n<xsl:choose>\n<xsl:when test=\"$dateString != '' and string-length($dateString)=10 and string(number(substring($dateString, 1, 4))) != 'NaN' \">\n<xsl:value-of select=\"concat(substring($dateString, 9, 2), '.', substring($dateString, 6, 2), '.', substring($dateString, 1, 4))\" />\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$dateString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"booleanCheckboxToString\">\n<xsl:param name=\"boolValue\" />\n<xsl:variable name=\"boolValueString\" select=\"string($boolValue)\" />\n<xsl:choose>\n<xsl:when test=\"$boolValueString = 'true' \">\n<xsl:text>Áno</xsl:text>\n</xsl:when>\n<xsl:when test=\"$boolValueString = 'false' \">\n<xsl:text>Nie</xsl:text>\n</xsl:when>\n<xsl:when test=\"$boolValueString = '1' \">\n<xsl:text>Áno</xsl:text>\n</xsl:when>\n<xsl:when test=\"$boolValueString = '0' \">\n<xsl:text>Nie</xsl:text>\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$boolValueString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"formatTimeTrimSeconds\">\n<xsl:param name=\"time\" />\n<xsl:variable name=\"timeString\" select=\"string($time)\" />\n<xsl:if test=\"$timeString != ''\">\n<xsl:value-of select=\"substring($timeString, 1, 5)\" />\n</xsl:if>\n</xsl:template>\n<xsl:template name=\"formatTime\">\n<xsl:param name=\"time\" />\n<xsl:variable name=\"timeString\" select=\"string($time)\" />\n<xsl:if test=\"$timeString != ''\">\n<xsl:value-of select=\"substring($timeString, 1, 8)\" />\n</xsl:if>\n</xsl:template>\n<xsl:template name=\"string-replace-all\">\n<xsl:param name=\"text\"/>\n<xsl:param name=\"replace\"/>\n<xsl:param name=\"by\"/>\n<xsl:choose>\n<xsl:when test=\"contains($text, $replace)\">\n<xsl:value-of select=\"substring-before($text,$replace)\"/>\n<xsl:value-of select=\"$by\"/>\n<xsl:call-template name=\"string-replace-all\">\n<xsl:with-param name=\"text\" select=\"substring-after($text,$replace)\"/>\n<xsl:with-param name=\"replace\" select=\"$replace\"/>\n<xsl:with-param name=\"by\" select=\"$by\" />\n</xsl:call-template>\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$text\"/>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"formatToSkDateTime\">\n<xsl:param name=\"dateTime\" />\n<xsl:variable name=\"dateTimeString\" select=\"string($dateTime)\" />\n<xsl:choose>\n<xsl:when test=\"$dateTimeString!= '' and string-length($dateTimeString)>18 and string(number(substring($dateTimeString, 1, 4))) != 'NaN' \">\n<xsl:value-of select=\"concat(substring($dateTimeString, 9, 2), '.', substring($dateTimeString, 6, 2), '.', substring($dateTimeString, 1, 4),' ', substring($dateTimeString, 12, 2),':', substring($dateTimeString, 15, 2))\" />\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$dateTimeString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"formatToSkDateTimeSecond\">\n<xsl:param name=\"dateTime\" />\n<xsl:variable name=\"dateTimeString\" select=\"string($dateTime)\" />\n<xsl:choose>\n<xsl:when test=\"$dateTimeString!= '' and string-length($dateTimeString)>18 and string(number(substring($dateTimeString, 1, 4))) != 'NaN' \">\n<xsl:value-of select=\"concat(substring($dateTimeString, 9, 2), '.', substring($dateTimeString, 6, 2), '.', substring($dateTimeString, 1, 4),' ', substring($dateTimeString, 12, 2),':', substring($dateTimeString, 15, 2),':', substring($dateTimeString, 18, 2))\" />\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$dateTimeString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n</xsl:stylesheet>\n";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                transformation,
                null,
                false,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXmlWithoutXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <subject>Nové podanie</subject>\n  <text>Podávam toto nové podanie.</text>\n";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXmlInvalidXmlAgainstXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <subject>Nové podanie</subject>\n  <text>Podávam toto nové podanie.</text>\n";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>\n";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXmlValidXmlAgainstXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n  <subject>Nové podanie</subject>\n  <text>Podávam toto nové podanie.</text>\n</GeneralAgenda>";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>\n";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXmlBase64WithoutXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KICA8c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0PgogIDx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+CjwvR2VuZXJhbEFnZW5kYT4=";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/xml;base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXmlBase64InvalidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KICA8c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0PgogIDx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+Cg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHhzOnNjaGVtYSBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHRhcmdldE5hbWVzcGFjZT0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KPHhzOnNpbXBsZVR5cGUgbmFtZT0idGV4dEFyZWEiPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJtZW5vIj4KPHhzOnJlc3RyaWN0aW9uIGJhc2U9InhzOnN0cmluZyI+CjwveHM6cmVzdHJpY3Rpb24+CjwveHM6c2ltcGxlVHlwZT4KPHhzOmVsZW1lbnQgbmFtZT0iR2VuZXJhbEFnZW5kYSI+Cjx4czpjb21wbGV4VHlwZT4KPHhzOnNlcXVlbmNlPgo8eHM6ZWxlbWVudCBuYW1lPSJzdWJqZWN0IiB0eXBlPSJtZW5vIiBtaW5PY2N1cnM9IjAiIG5pbGxhYmxlPSJ0cnVlIiAvPgo8eHM6ZWxlbWVudCBuYW1lPSJ0ZXh0IiB0eXBlPSJ0ZXh0QXJlYSIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPC94czpzZXF1ZW5jZT4KPC94czpjb21wbGV4VHlwZT4KPC94czplbGVtZW50Pgo8L3hzOnNjaGVtYT4K";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/xml;base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXmlBase64ValidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KICA8c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0PgogIDx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+CjwvR2VuZXJhbEFnZW5kYT4=";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHhzOnNjaGVtYSBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHRhcmdldE5hbWVzcGFjZT0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KPHhzOnNpbXBsZVR5cGUgbmFtZT0idGV4dEFyZWEiPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJtZW5vIj4KPHhzOnJlc3RyaWN0aW9uIGJhc2U9InhzOnN0cmluZyI+CjwveHM6cmVzdHJpY3Rpb24+CjwveHM6c2ltcGxlVHlwZT4KPHhzOmVsZW1lbnQgbmFtZT0iR2VuZXJhbEFnZW5kYSI+Cjx4czpjb21wbGV4VHlwZT4KPHhzOnNlcXVlbmNlPgo8eHM6ZWxlbWVudCBuYW1lPSJzdWJqZWN0IiB0eXBlPSJtZW5vIiBtaW5PY2N1cnM9IjAiIG5pbGxhYmxlPSJ0cnVlIiAvPgo8eHM6ZWxlbWVudCBuYW1lPSJ0ZXh0IiB0eXBlPSJ0ZXh0QXJlYSIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPC94czpzZXF1ZW5jZT4KPC94czpjb21wbGV4VHlwZT4KPC94czplbGVtZW50Pgo8L3hzOnNjaGVtYT4K";
        var signingParameters = new ServerSigningParameters(
                SignatureLevel.XAdES_BASELINE_B,
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
                null);

        var payloadMimeType = "application/xml;base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }
}