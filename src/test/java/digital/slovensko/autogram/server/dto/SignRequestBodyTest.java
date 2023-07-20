package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

class SignRequestBodyTest {

    @Test
    void testValidateXDCWithoutXSD() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("xdc.xml").readAllBytes());
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
        var xmlContent = new String(this.getClass().getResourceAsStream("xdc.xml").readAllBytes());
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
    void testValidateXDCDigestMismatchXSD() {
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
    void testValidateXDCDigestMismatchXSLT() {
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
    void testValidateXDCInvalidXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:complexType>\n</xs:element>\n</xs:schema>";
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

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCInvalidXSLT() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var transformation = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  xmlns:egonp=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xsl:output method=\"text\" indent=\"yes\" omit-xml-declaration=\"yes\"/>\n<xsl:strip-space elements=\"*\" />\n<xsl:template match=\"egonp:GeneralAgenda\">\n<xsl:text>Všeobecná agenda</xsl:text>\n<xsl:apply-templates/>\n</xsl:template>\n<xsl:template match=\"egonp:GeneralAgenda/egonp:subject\">\n<xsl:if test=\"./text()\">\n<xsl:text>&#xA;</xsl:text>\n<xsl:text>&#09;</xsl:text><xsl:text>Predmet: </xsl:text><xsl:call-template name=\"string-replace-all\"><xsl:with-param name=\"text\" select=\".\" /><xsl:with-param name=\"replace\" select=\"'&#10;'\" /><xsl:with-param name=\"by\" select=\"'&#13;&#10;&#09;'\" /></xsl:call-template>\n</xsl:if>\n</xsl:template>\n<xsl:template match=\"egonp:GeneralAgenda/egonp:text\">\n<xsl:if test=\"./text()\">\n<xsl:text>&#xA;</xsl:text>\n<xsl:text>&#09;</xsl:text><xsl:text>Text: </xsl:text><xsl:call-template name=\"string-replace-all\"><xsl:with-param name=\"text\" select=\".\" /><xsl:with-param name=\"replace\" select=\"'&#10;'\" /><xsl:with-param name=\"by\" select=\"'&#13;&#10;&#09;'\" /></xsl:call-template>\n</xsl:if>\n</xsl:template>\n<xsl:template name=\"formatToSkDate\">\n<xsl:param name=\"date\" />\n<xsl:variable name=\"dateString\" select=\"string($date)\" />\n<xsl:choose>\n<xsl:when test=\"$dateString != '' and string-length($dateString)=10 and string(number(substring($dateString, 1, 4))) != 'NaN' \">\n<xsl:value-of select=\"concat(substring($dateString, 9, 2), '.', substring($dateString, 6, 2), '.', substring($dateString, 1, 4))\" />\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$dateString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"booleanCheckboxToString\">\n<xsl:param name=\"boolValue\" />\n<xsl:variable name=\"boolValueString\" select=\"string($boolValue)\" />\n<xsl:choose>\n<xsl:when test=\"$boolValueString = 'true' \">\n<xsl:text>Áno</xsl:text>\n</xsl:when>\n<xsl:when test=\"$boolValueString = 'false' \">\n<xsl:text>Nie</xsl:text>\n</xsl:when>\n<xsl:when test=\"$boolValueString = '1' \">\n<xsl:text>Áno</xsl:text>\n</xsl:when>\n<xsl:when test=\"$boolValueString = '0' \">\n<xsl:text>Nie</xsl:text>\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$boolValueString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"formatTimeTrimSeconds\">\n<xsl:param name=\"time\" />\n<xsl:variable name=\"timeString\" select=\"string($time)\" />\n<xsl:if test=\"$timeString != ''\">\n<xsl:value-of select=\"substring($timeString, 1, 5)\" />\n</xsl:if>\n</xsl:template>\n<xsl:template name=\"formatTime\">\n<xsl:param name=\"time\" />\n<xsl:variable name=\"timeString\" select=\"string($time)\" />\n<xsl:if test=\"$timeString != ''\">\n<xsl:value-of select=\"substring($timeString, 1, 8)\" />\n</xsl:if>\n</xsl:template>\n<xsl:template name=\"string-replace-all\">\n<xsl:param name=\"text\"/>\n<xsl:param name=\"replace\"/>\n<xsl:param name=\"by\"/>\n<xsl:choose>\n<xsl:when test=\"contains($text, $replace)\">\n<xsl:value-of select=\"substring-before($text,$replace)\"/>\n<xsl:value-of select=\"$by\"/>\n<xsl:call-template name=\"string-replace-all\">\n<xsl:with-param name=\"text\" select=\"substring-after($text,$replace)\"/>\n<xsl:with-param name=\"replace\" select=\"$replace\"/>\n<xsl:with-param name=\"by\" select=\"$by\" />\n</xsl:call-template>\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$text\"/>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"formatToSkDateTime\">\n<xsl:param name=\"dateTime\" />\n<xsl:variable name=\"dateTimeString\" select=\"string($dateTime)\" />\n<xsl:choose>\n<xsl:when test=\"$dateTimeString!= '' and string-length($dateTimeString)>18 and string(number(substring($dateTimeString, 1, 4))) != 'NaN' \">\n<xsl:value-of select=\"concat(substring($dateTimeString, 9, 2), '.', substring($dateTimeString, 6, 2), '.', substring($dateTimeString, 1, 4),' ', substring($dateTimeString, 12, 2),':', substring($dateTimeString, 15, 2))\" />\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$dateTimeString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:choose>\n</xsl:template>\n<xsl:template name=\"formatToSkDateTimeSecond\">\n<xsl:param name=\"dateTime\" />\n<xsl:variable name=\"dateTimeString\" select=\"string($dateTime)\" />\n<xsl:choose>\n<xsl:when test=\"$dateTimeString!= '' and string-length($dateTimeString)>18 and string(number(substring($dateTimeString, 1, 4))) != 'NaN' \">\n<xsl:value-of select=\"concat(substring($dateTimeString, 9, 2), '.', substring($dateTimeString, 6, 2), '.', substring($dateTimeString, 1, 4),' ', substring($dateTimeString, 12, 2),':', substring($dateTimeString, 15, 2),':', substring($dateTimeString, 18, 2))\" />\n</xsl:when>\n<xsl:otherwise>\n<xsl:value-of select=\"$dateTimeString\"></xsl:value-of>\n</xsl:otherwise>\n</xsl:template>\n</xsl:stylesheet>\n";
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

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCEmptyXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var schema = "";
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

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCEmptyXSLT() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var transformation = "";
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

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64InvalidXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64InvalidXSLT() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var transformation = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHhzbDpzdHlsZXNoZWV0IHZlcnNpb249IjEuMCIgeG1sbnM6eHNsPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L1hTTC9UcmFuc2Zvcm0iICB4bWxuczplZ29ucD0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KPHhzbDpvdXRwdXQgbWV0aG9kPSJ0ZXh0IiBpbmRlbnQ9InllcyIgb21pdC14bWwtZGVjbGFyYXRpb249InllcyIvPgo8eHNsOnN0cmlwLXNwYWNlIGVsZW1lbnRzPSIqIiAvPgo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhIj4KPHhzbDp0ZXh0PlbFoWVvYmVjbsOhIGFnZW5kYTwveHNsOnRleHQ+Cjx4c2w6YXBwbHktdGVtcGxhdGVzLz4KPC94c2w6dGVtcGxhdGU+Cjx4c2w6dGVtcGxhdGUgbWF0Y2g9ImVnb25wOkdlbmVyYWxBZ2VuZGEvZWdvbnA6c3ViamVjdCI+Cjx4c2w6aWYgdGVzdD0iLi90ZXh0KCkiPgo8eHNsOnRleHQ+JiN4QTs8L3hzbDp0ZXh0Pgo8eHNsOnRleHQ+PHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJyYjMTA7JyIgLz48eHNsOndpdGgtcGFyYW0gbmFtZT0iYnkiIHNlbGVjdD0iJyYjMTM7JiMxMDsmIzA5OyciIC8+PC94c2w6Y2FsbC10ZW1wbGF0ZT4KPC94c2w6aWY+CjwveHNsOnRlbXBsYXRlPgo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhL2Vnb25wOnRleHQiPgo8eHNsOmlmIHRlc3Q9Ii4vdGV4dCgpIj4KPHhzbDp0ZXh0PiYjeEE7PC94c2w6dGV4dD4KPHhzbDp0ZXh0PiYjMDk7PC94c2w6dGV4dD48eHNsOnRleHQ+VGV4dDogPC94c2w6dGV4dD48eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj48eHNsOndpdGgtcGFyYW0gbmFtZT0idGV4dCIgc2VsZWN0PSIuIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IicmIzEwOyciIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IicmIzEzOyYjMTA7JiMwOTsnIiAvPjwveHNsOmNhbGwtdGVtcGxhdGU+CjwveHNsOmlmPgo8L3hzbDp0ZW1wbGF0ZT4KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUb1NrRGF0ZSI+Cjx4c2w6cGFyYW0gbmFtZT0iZGF0ZSIgLz4KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkZGF0ZSkiIC8+Cjx4c2w6Y2hvb3NlPgo8eHNsOndoZW4gdGVzdD0iJGRhdGVTdHJpbmcgIT0gJycgYW5kIHN0cmluZy1sZW5ndGgoJGRhdGVTdHJpbmcpPTEwIGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVN0cmluZywgMSwgNCkpKSAhPSAnTmFOJyAiPgo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iY29uY2F0KHN1YnN0cmluZygkZGF0ZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVTdHJpbmcsIDEsIDQpKSIgLz4KPC94c2w6d2hlbj4KPHhzbDpvdGhlcndpc2U+Cjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkZGF0ZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+CjwveHNsOm90aGVyd2lzZT4KPC94c2w6Y2hvb3NlPgo8L3hzbDp0ZW1wbGF0ZT4KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJib29sZWFuQ2hlY2tib3hUb1N0cmluZyI+Cjx4c2w6cGFyYW0gbmFtZT0iYm9vbFZhbHVlIiAvPgo8eHNsOnZhcmlhYmxlIG5hbWU9ImJvb2xWYWx1ZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGJvb2xWYWx1ZSkiIC8+Cjx4c2w6Y2hvb3NlPgo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICd0cnVlJyAiPgo8eHNsOnRleHQ+w4FubzwveHNsOnRleHQ+CjwveHNsOndoZW4+Cjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJ2ZhbHNlJyAiPgo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4KPC94c2w6d2hlbj4KPHhzbDp3aGVuIHRlc3Q9IiRib29sVmFsdWVTdHJpbmcgPSAnMScgIj4KPHhzbDp0ZXh0PsOBbm88L3hzbDp0ZXh0Pgo8L3hzbDp3aGVuPgo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICcwJyAiPgo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4KPC94c2w6d2hlbj4KPHhzbDpvdGhlcndpc2U+Cjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYm9vbFZhbHVlU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4KPC94c2w6b3RoZXJ3aXNlPgo8L3hzbDpjaG9vc2U+CjwveHNsOnRlbXBsYXRlPgo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRpbWVUcmltU2Vjb25kcyI+Cjx4c2w6cGFyYW0gbmFtZT0idGltZSIgLz4KPHhzbDp2YXJpYWJsZSBuYW1lPSJ0aW1lU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkdGltZSkiIC8+Cjx4c2w6aWYgdGVzdD0iJHRpbWVTdHJpbmcgIT0gJyciPgo8eHNsOnZhbHVlLW9mIHNlbGVjdD0ic3Vic3RyaW5nKCR0aW1lU3RyaW5nLCAxLCA1KSIgLz4KPC94c2w6aWY+CjwveHNsOnRlbXBsYXRlPgo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRpbWUiPgo8eHNsOnBhcmFtIG5hbWU9InRpbWUiIC8+Cjx4c2w6dmFyaWFibGUgbmFtZT0idGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJHRpbWUpIiAvPgo8eHNsOmlmIHRlc3Q9IiR0aW1lU3RyaW5nICE9ICcnIj4KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZygkdGltZVN0cmluZywgMSwgOCkiIC8+CjwveHNsOmlmPgo8L3hzbDp0ZW1wbGF0ZT4KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPgo8eHNsOnBhcmFtIG5hbWU9InRleHQiLz4KPHhzbDpwYXJhbSBuYW1lPSJyZXBsYWNlIi8+Cjx4c2w6cGFyYW0gbmFtZT0iYnkiLz4KPHhzbDpjaG9vc2U+Cjx4c2w6d2hlbiB0ZXN0PSJjb250YWlucygkdGV4dCwgJHJlcGxhY2UpIj4KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZy1iZWZvcmUoJHRleHQsJHJlcGxhY2UpIi8+Cjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYnkiLz4KPHhzbDpjYWxsLXRlbXBsYXRlIG5hbWU9InN0cmluZy1yZXBsYWNlLWFsbCI+Cjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9InN1YnN0cmluZy1hZnRlcigkdGV4dCwkcmVwbGFjZSkiLz4KPHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJHJlcGxhY2UiLz4KPHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IiRieSIgLz4KPC94c2w6Y2FsbC10ZW1wbGF0ZT4KPC94c2w6d2hlbj4KPHhzbDpvdGhlcndpc2U+Cjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkdGV4dCIvPgo8L3hzbDpvdGhlcndpc2U+CjwveHNsOmNob29zZT4KPC94c2w6dGVtcGxhdGU+Cjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VG9Ta0RhdGVUaW1lIj4KPHhzbDpwYXJhbSBuYW1lPSJkYXRlVGltZSIgLz4KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlVGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGRhdGVUaW1lKSIgLz4KPHhzbDpjaG9vc2U+Cjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVRpbWVTdHJpbmchPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVRpbWVTdHJpbmcpPjE4IGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEsIDQpKSkgIT0gJ05hTicgIj4KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9ImNvbmNhdChzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA5LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSwnICcsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEyLCAyKSwnOicsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDE1LCAyKSkiIC8+CjwveHNsOndoZW4+Cjx4c2w6b3RoZXJ3aXNlPgo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGRhdGVUaW1lU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4KPC94c2w6b3RoZXJ3aXNlPgo8L3hzbDpjaG9vc2U+CjwveHNsOnRlbXBsYXRlPgo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRvU2tEYXRlVGltZVNlY29uZCI+Cjx4c2w6cGFyYW0gbmFtZT0iZGF0ZVRpbWUiIC8+Cjx4c2w6dmFyaWFibGUgbmFtZT0iZGF0ZVRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCRkYXRlVGltZSkiIC8+Cjx4c2w6Y2hvb3NlPgo8eHNsOndoZW4gdGVzdD0iJGRhdGVUaW1lU3RyaW5nIT0gJycgYW5kIHN0cmluZy1sZW5ndGgoJGRhdGVUaW1lU3RyaW5nKT4xOCBhbmQgc3RyaW5nKG51bWJlcihzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSkpICE9ICdOYU4nICI+Cjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJjb25jYXQoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgNiwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCksJyAnLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxMiwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxNSwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxOCwgMikpIiAvPgo8L3hzbDp3aGVuPgo8eHNsOm90aGVyd2lzZT4KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9IiRkYXRlVGltZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+CjwveHNsOm90aGVyd2lzZT4KPC94c2w6Y2hvb3NlPgo8L3hzbDp0ZW1wbGF0ZT4KPC94c2w6c3R5bGVzaGVldD4KCg==";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64EmptyXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64EmptyXSLT() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var transformation = "";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64DigestMismatchXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQxMTExMVVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64DigestMismatchXSLT() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var transformation = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqMTExMTF5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(RequestValidationException.class, signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64WithoutXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64WithValidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(signRequestBody::getParameters);
    }

    @Test
    void testValidateXDCBase64WithInvalidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC94ZGM6WE1MRGF0YT48eGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48eGRjOlVzZWRYU0RSZWZlcmVuY2UgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSIvQ3RuMEI5RDdIS242VVJGUjhpUFVLZnlHZTRtQllwSysyNWRjMWlZV3VFPSIgVHJhbnNmb3JtQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy9UUi8yMDAxL1JFQy14bWwtYzE0bi0yMDAxMDMxNSI+aHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45L2Zvcm0ueHNkPC94ZGM6VXNlZFhTRFJlZmVyZW5jZT48eGRjOlVzZWRQcmVzZW50YXRpb25TY2hlbWFSZWZlcmVuY2UgQ29udGVudFR5cGU9ImFwcGxpY2F0aW9uL3hzbHQreG1sIiBEaWdlc3RNZXRob2Q9InVybjpvaWQ6Mi4xNi44NDAuMS4xMDEuMy40LjIuMSIgRGlnZXN0VmFsdWU9IlFvMWpZWDFKV3lkdk0vT0wvcm5pcnBoazFyTTF6NDFmUFJYQkVncC9xYmc9IiBMYW5ndWFnZT0ic2siIE1lZGlhRGVzdGluYXRpb25UeXBlRGVzY3JpcHRpb249IlRYVCIgVHJhbnNmb3JtQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy9UUi8yMDAxL1JFQy14bWwtYzE0bi0yMDAxMDMxNSI+aHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45L2Zvcm0ueHNsdDwveGRjOlVzZWRQcmVzZW50YXRpb25TY2hlbWFSZWZlcmVuY2U+PC94ZGM6VXNlZFNjaGVtYXNSZWZlcmVuY2VkPjwveGRjOlhNTERhdGFDb250YWluZXI+";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
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

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

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