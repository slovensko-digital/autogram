package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Base64;

class SignRequestBodyTest {
    private static String xsdSchema;
    private static String xsltTransformation;
    private static String defaultIdentifier;

    @BeforeAll
    static void setDefaultValues() throws IOException {
        xsdSchema = new String(SignRequestBodyTest.class.getResourceAsStream("../../general_agenda.xsd").readAllBytes());
        xsltTransformation = new String(SignRequestBodyTest.class.getResourceAsStream("../../general_agenda.xslt").readAllBytes());
        defaultIdentifier = "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9";
    }

    @Test
    void testValidateXDCWithoutXSD() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("xdc.xml").readAllBytes());
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCWithValidXmlAgainstXSD() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("xdc.xml").readAllBytes());
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                xsdSchema,
                null,
                null,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCWithInvalidXmlAgainstXSD() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("testValidateXDCWithInvalidXmlAgainstXSD-xmlContent.xml").readAllBytes());
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        var signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCDigestMismatchXSD() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("testValidateXDCDigestMismatchXSD-xmlContent.xml").readAllBytes());
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCDigestMismatchXSLT() throws IOException {
        var xmlContent = new String(this.getClass().getResourceAsStream("testValidateXDCDigestMismatchXSLT-xmlContent.xml").readAllBytes());
        var document = new Document(xmlContent);

        var transformation = new String(this.getClass().getResourceAsStream("testValidateXDCDigestMismatchXSLT-xslt.xml").readAllBytes());
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInvalidXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:complexType>\n</xs:element>\n</xs:schema>";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInvalidXSLT() throws IOException {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var transformation = new String(Base64.getDecoder().decode("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjx4c2w6c3R5bGVzaGVldCB2ZXJzaW9uPSIxLjAiICB4bWxuczp4c2w9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvWFNML1RyYW5zZm9ybSIgIHhtbG5zOmVnb25wPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIGV4Y2x1ZGUtcmVzdWx0LXByZWZpeGVzPSJlZ29ucCI+DQo8eHNsOm91dHB1dCBtZXRob2Q9Imh0bWwiIGRvY3R5cGUtc3lzdGVtPSJodHRwOi8vd3d3LnczLm9yZy9UUi9odG1sNC9zdHJpY3QuZHRkIiBkb2N0eXBlLXB1YmxpYz0iLS8vVzNDLy9EVEQgSFRNTCA0LjAxLy9FTiIgaW5kZW50PSJubyIgb21pdC14bWwtZGVjbGFyYXRpb249InllcyIvPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iLyI+DQo8aHRtbD4NCjxoZWFkPg0KPG1ldGEgaHR0cC1lcXVpdj0iWC1VQS1Db21wYXRpYmxlIiBjb250ZW50PSJJRT04IiAvPg0KPHRpdGxlPlbFoWVvYmVjbsOhIGFnZW5kYTwvdGl0bGU+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD1VVEYtOCIvPg0KPG1ldGEgbmFtZT0ibGFuZ3VhZ2UiIGNvbnRlbnQ9InNrLVNLIi8+DQo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPg0KYm9keSB7IA0KCWZvbnQtZmFtaWx5OiAnT3BlbiBTYW5zJywgJ1NlZ29lIFVJJywgJ1RyZWJ1Y2hldCBNUycsICdHZW5ldmEgQ0UnLCBsdWNpZGEsIHNhbnMtc2VyaWY7DQoJYmFja2dyb3VuZCA6ICNmZmZmZmYgIWltcG9ydGFudCA7DQp9DQoudWktdGFicyB7DQoJcGFkZGluZzogLjJlbTsNCglwb3NpdGlvbjogcmVsYXRpdmU7DQoJem9vbTogMTsNCn0JCQkJCQkJCQ0KLmNsZWFyIHsgY2xlYXI6IGJvdGg7IGhlaWdodDogMDt9DQoubGF5b3V0TWFpbiB7DQoJbWFyZ2luOiAwcHggYXV0bzsNCglwYWRkaW5nOiA1cHggNXB4IDVweCA1cHg7CQ0KfQkJCQkNCi5sYXlvdXRSb3cgeyBtYXJnaW4tYm90dG9tOiA1cHg7IH0JCQkJDQouY2FwdGlvbiB7IC8qd2lkdGg6IDEwMCU7IGJvcmRlci1ib3R0b206IHNvbGlkIDFweCBibGFjazsqLyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24geyBib3JkZXI6IDBweCAhaW1wb3J0YW50OyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24gc3BhbiB7DQoJYmFja2dyb3VuZDogbm9uZSAhaW1wb3J0YW50Ow0KCWRpc3BsYXk6IG5vbmU7DQp9IA0KLmNhcHRpb24gLnRpdGxlIHsgcGFkZGluZy1sZWZ0OiA1cHg7IH0NCi5oZWFkZXJjb3JyZWN0aW9uIHsJDQoJbWFyZ2luOiAwcHg7DQogICAgZm9udC1zaXplIDogMWVtOw0KICAgIGZvbnQtd2VpZ2h0OiBib2xkOw0KfQkJCQkNCi5sYWJlbFZpcyB7DQoJZmxvYXQ6IGxlZnQ7DQoJZm9udC13ZWlnaHQ6IGJvbGQ7DQoJZm9udC1mYW1pbHk6ICdPcGVuIFNhbnMnLCAnU2Vnb2UgVUknLCAnVHJlYnVjaGV0IE1TJywgJ0dlbmV2YSBDRScsIGx1Y2lkYSwgc2Fucy1zZXJpZjsNCglsaW5lLWhlaWdodDogMjVweDsNCgltYXJnaW46IDBweCAxOHB4IDBweCAwcHg7DQoJcGFkZGluZy1sZWZ0OiAzcHg7DQoJd2lkdGg6IDE5MHB4Ow0KCXdvcmQtd3JhcDogYnJlYWstd29yZDsNCiAgICBmb250LXNpemU6IDAuOGVtOw0KfQ0KLmNvbnRlbnRWaXMgeyAgICAJICAgICANCglmbG9hdDogbGVmdDsJDQoJbGluZS1oZWlnaHQ6IDI1cHg7DQoJbWFyZ2luOiAwcHg7DQoJcGFkZGluZzogMHB4Ow0KCXZlcnRpY2FsLWFsaWduOiB0b3A7DQogICAgZm9udC1zaXplOiAwLjc1ZW07CQkJDQp9DQoud29yZHdyYXAgeyANCiAgICB3aGl0ZS1zcGFjZTogcHJlLXdyYXA7ICAgICAgDQogICAgd2hpdGUtc3BhY2U6IC1tb3otcHJlLXdyYXA7IA0KICAgIHdoaXRlLXNwYWNlOiAtcHJlLXdyYXA7ICAgICANCiAgICB3aGl0ZS1zcGFjZTogLW8tcHJlLXdyYXA7ICAgDQogICAgd29yZC13cmFwOiBicmVhay13b3JkOyAgICAgIA0KfQkNCi51aS13aWRnZXQtY29udGVudCB7DQoJYmFja2dyb3VuZCA6IDUwJSA1MCUgcmVwZWF0LXggI2ZmZmZmZjsNCglib3JkZXIgOiAjZDRkNGQ0IHNvbGlkIDJweDsNCgljb2xvciA6ICM0ZjRlNGU7DQoJYm9yZGVyLXJhZGl1cyA6IDNweDsNCn0NCi51aS13aWRnZXQtaGVhZGVyIHsNCgljdXJzb3IgOiBwb2ludGVyOw0KCWZvbnQtc2l6ZSA6IDAuOGVtOw0KCWNvbG9yIDogIzQ5NDk0OTsNCglwYWRkaW5nLWxlZnQgOiAycHg7DQoJYm9yZGVyIDogI2VhZTllOCBzb2xpZCAxcHg7DQoJYmFja2dyb3VuZC1jb2xvciA6ICNlYWU5ZTg7DQoJbWFyZ2luLWJvdHRvbTogM3B4Ow0KCWJvcmRlci1yYWRpdXMgOiAzcHg7DQp9CQkJDQo8L3N0eWxlPg0KPC9oZWFkPg0KPGJvZHk+DQo8ZGl2IGlkPSJtYWluIiBjbGFzcz0ibGF5b3V0TWFpbiI+DQo8eHNsOmFwcGx5LXRlbXBsYXRlcy8+DQo8L2Rpdj4NCjwvYm9keT4NCjwvaHRtbD4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iL2Vnb25wOkdlbmVyYWxBZ2VuZGEiPg0KPGRpdiBjbGFzcz0ibGF5b3V0Um93IHVpLXRhYnMgdWktd2lkZ2V0LWNvbnRlbnQiID4NCjxkaXYgY2xhc3M9ImNhcHRpb24gdWktd2lkZ2V0LWhlYWRlciI+DQo8ZGl2IGNsYXNzPSJoZWFkZXJjb3JyZWN0aW9uIj5WxaFlb2JlY27DoSBhZ2VuZGE8L2Rpdj4NCjwvZGl2Pg0KPHhzbDphcHBseS10ZW1wbGF0ZXMgc2VsZWN0PSIuL2Vnb25wOnN1YmplY3QiLz4NCjx4c2w6YXBwbHktdGVtcGxhdGVzIHNlbGVjdD0iLi9lZ29ucDp0ZXh0Ii8+DQo8L2Rpdj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iZWdvbnA6R2VuZXJhbEFnZW5kYS9lZ29ucDpzdWJqZWN0Ij4NCjx4c2w6aWYgdGVzdD0iLi90ZXh0KCkiPg0KPGRpdj48bGFiZWwgY2xhc3M9ImxhYmVsVmlzIj5QcmVkbWV0OiA8L2xhYmVsPjxzcGFuIGNsYXNzPSJjb250ZW50VmlzIHdvcmR3cmFwIj48eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj48eHNsOndpdGgtcGFyYW0gbmFtZT0idGV4dCIgc2VsZWN0PSIuIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiclMEEnIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJieSIgc2VsZWN0PSInJiMxMzsmIzEwOyciIC8+PC94c2w6Y2FsbC10ZW1wbGF0ZT48L3NwYW4+PC9kaXY+PGRpdiBjbGFzcz0iY2xlYXIiPiYjeGEwOzwvZGl2PjwveHNsOmlmPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhL2Vnb25wOnRleHQiPg0KPHhzbDppZiB0ZXN0PSIuL3RleHQoKSI+DQo8ZGl2PjxsYWJlbCBjbGFzcz0ibGFiZWxWaXMiPlRleHQ6IDwvbGFiZWw+PHNwYW4gY2xhc3M9ImNvbnRlbnRWaXMgd29yZHdyYXAiPjx4c2w6Y2FsbC10ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9Ii4iIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJyUwQSciIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IicmIzEzOyYjMTA7JyIgLz48L3hzbDpjYWxsLXRlbXBsYXRlPjwvc3Bhbj48L2Rpdj48ZGl2IGNsYXNzPSJjbGVhciI+JiN4YTA7PC9kaXY+PC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VG9Ta0RhdGUiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkZGF0ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVN0cmluZyAhPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVN0cmluZyk9MTAgYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCAxLCA0KSkpICE9ICdOYU4nICI+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iY29uY2F0KHN1YnN0cmluZygkZGF0ZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVTdHJpbmcsIDEsIDQpKSIgLz4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkZGF0ZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iYm9vbGVhbkNoZWNrYm94VG9TdHJpbmciPg0KPHhzbDpwYXJhbSBuYW1lPSJib29sVmFsdWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9ImJvb2xWYWx1ZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGJvb2xWYWx1ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJ3RydWUnICI+DQo8eHNsOnRleHQ+w4FubzwveHNsOnRleHQ+DQo8L3hzbDp3aGVuPg0KPHhzbDp3aGVuIHRlc3Q9IiRib29sVmFsdWVTdHJpbmcgPSAnZmFsc2UnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICcxJyAiPg0KPHhzbDp0ZXh0PsOBbm88L3hzbDp0ZXh0Pg0KPC94c2w6d2hlbj4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJzAnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYm9vbFZhbHVlU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUaW1lVHJpbVNlY29uZHMiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0aW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJ0aW1lU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkdGltZSkiIC8+DQo8eHNsOmlmIHRlc3Q9IiR0aW1lU3RyaW5nICE9ICcnIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJzdWJzdHJpbmcoJHRpbWVTdHJpbmcsIDEsIDUpIiAvPg0KPC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9InRpbWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9InRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCR0aW1lKSIgLz4NCjx4c2w6aWYgdGVzdD0iJHRpbWVTdHJpbmcgIT0gJyciPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZygkdGltZVN0cmluZywgMSwgOCkiIC8+DQo8L3hzbDppZj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0ZXh0Ii8+DQo8eHNsOnBhcmFtIG5hbWU9InJlcGxhY2UiLz4NCjx4c2w6cGFyYW0gbmFtZT0iYnkiLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9ImNvbnRhaW5zKCR0ZXh0LCAkcmVwbGFjZSkiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZy1iZWZvcmUoJHRleHQsJHJlcGxhY2UpIi8+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGJ5Ii8+DQo8eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9InN1YnN0cmluZy1hZnRlcigkdGV4dCwkcmVwbGFjZSkiLz4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiRyZXBsYWNlIi8+DQo8eHNsOndpdGgtcGFyYW0gbmFtZT0iYnkiIHNlbGVjdD0iJGJ5IiAvPg0KPC94c2w6Y2FsbC10ZW1wbGF0ZT4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkdGV4dCIvPg0KPC94c2w6b3RoZXJ3aXNlPg0KPC94c2w6Y2hvb3NlPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRvU2tEYXRlVGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9ImRhdGVUaW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlVGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGRhdGVUaW1lKSIgLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9IiRkYXRlVGltZVN0cmluZyE9ICcnIGFuZCBzdHJpbmctbGVuZ3RoKCRkYXRlVGltZVN0cmluZyk+MTggYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCkpKSAhPSAnTmFOJyAiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9ImNvbmNhdChzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA5LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSwnICcsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEyLCAyKSwnOicsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDE1LCAyKSkiIC8+DQo8L3hzbDp3aGVuPg0KPHhzbDpvdGhlcndpc2U+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGRhdGVUaW1lU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUb1NrRGF0ZVRpbWVTZWNvbmQiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlVGltZSIgLz4NCjx4c2w6dmFyaWFibGUgbmFtZT0iZGF0ZVRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCRkYXRlVGltZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVRpbWVTdHJpbmchPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVRpbWVTdHJpbmcpPjE4IGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEsIDQpKSkgIT0gJ05hTicgIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJjb25jYXQoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgNiwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCksJyAnLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxMiwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxNSwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxOCwgMikpIiAvPg0KPC94c2w6d2hlbj4NCjx4c2w6b3RoZXJ3aXNlPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9IiRkYXRlVGltZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjwveHNsOnN0eWxlc2hlZXQ+DQoNCg=="));
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCEmptyXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var schema = "";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCEmptyXSLT() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var transformation = "";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(TransformationParsingErrorException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCMissingDigestvalues() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCMissingReferences() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda></xdc:XMLData><xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }


    @Test
    void testValidateXDCEmptyXmlData() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData ContentType=\"application/xml; charset=UTF-8\" Identifier=\"http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9\" Version=\"1.9\"></xdc:XMLData><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(XMLValidationException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64InvalidXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64InvalidXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64InvalidXSLT() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var transformation = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjx4c2w6c3R5bGVzaGVldCB2ZXJzaW9uPSIxLjAiICB4bWxuczp4c2w9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvWFNML1RyYW5zZm9ybSIgIHhtbG5zOmVnb25wPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIGV4Y2x1ZGUtcmVzdWx0LXByZWZpeGVzPSJlZ29ucCI+DQo8eHNsOm91dHB1dCBtZXRob2Q9Imh0bWwiIGRvY3R5cGUtc3lzdGVtPSJodHRwOi8vd3d3LnczLm9yZy9UUi9odG1sNC9zdHJpY3QuZHRkIiBkb2N0eXBlLXB1YmxpYz0iLS8vVzNDLy9EVEQgSFRNTCA0LjAxLy9FTiIgaW5kZW50PSJubyIgb21pdC14bWwtZGVjbGFyYXRpb249InllcyIvPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iLyI+DQo8aHRtbD4NCjxoZWFkPg0KPG1ldGEgaHR0cC1lcXVpdj0iWC1VQS1Db21wYXRpYmxlIiBjb250ZW50PSJJRT04IiAvPg0KPHRpdGxlPlbFoWVvYmVjbsOhIGFnZW5kYTwvdGl0bGU+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD1VVEYtOCIvPg0KPG1ldGEgbmFtZT0ibGFuZ3VhZ2UiIGNvbnRlbnQ9InNrLVNLIi8+DQo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPg0KYm9keSB7IA0KCWZvbnQtZmFtaWx5OiAnT3BlbiBTYW5zJywgJ1NlZ29lIFVJJywgJ1RyZWJ1Y2hldCBNUycsICdHZW5ldmEgQ0UnLCBsdWNpZGEsIHNhbnMtc2VyaWY7DQoJYmFja2dyb3VuZCA6ICNmZmZmZmYgIWltcG9ydGFudCA7DQp9DQoudWktdGFicyB7DQoJcGFkZGluZzogLjJlbTsNCglwb3NpdGlvbjogcmVsYXRpdmU7DQoJem9vbTogMTsNCn0JCQkJCQkJCQ0KLmNsZWFyIHsgY2xlYXI6IGJvdGg7IGhlaWdodDogMDt9DQoubGF5b3V0TWFpbiB7DQoJbWFyZ2luOiAwcHggYXV0bzsNCglwYWRkaW5nOiA1cHggNXB4IDVweCA1cHg7CQ0KfQkJCQkNCi5sYXlvdXRSb3cgeyBtYXJnaW4tYm90dG9tOiA1cHg7IH0JCQkJDQouY2FwdGlvbiB7IC8qd2lkdGg6IDEwMCU7IGJvcmRlci1ib3R0b206IHNvbGlkIDFweCBibGFjazsqLyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24geyBib3JkZXI6IDBweCAhaW1wb3J0YW50OyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24gc3BhbiB7DQoJYmFja2dyb3VuZDogbm9uZSAhaW1wb3J0YW50Ow0KCWRpc3BsYXk6IG5vbmU7DQp9IA0KLmNhcHRpb24gLnRpdGxlIHsgcGFkZGluZy1sZWZ0OiA1cHg7IH0NCi5oZWFkZXJjb3JyZWN0aW9uIHsJDQoJbWFyZ2luOiAwcHg7DQogICAgZm9udC1zaXplIDogMWVtOw0KICAgIGZvbnQtd2VpZ2h0OiBib2xkOw0KfQkJCQkNCi5sYWJlbFZpcyB7DQoJZmxvYXQ6IGxlZnQ7DQoJZm9udC13ZWlnaHQ6IGJvbGQ7DQoJZm9udC1mYW1pbHk6ICdPcGVuIFNhbnMnLCAnU2Vnb2UgVUknLCAnVHJlYnVjaGV0IE1TJywgJ0dlbmV2YSBDRScsIGx1Y2lkYSwgc2Fucy1zZXJpZjsNCglsaW5lLWhlaWdodDogMjVweDsNCgltYXJnaW46IDBweCAxOHB4IDBweCAwcHg7DQoJcGFkZGluZy1sZWZ0OiAzcHg7DQoJd2lkdGg6IDE5MHB4Ow0KCXdvcmQtd3JhcDogYnJlYWstd29yZDsNCiAgICBmb250LXNpemU6IDAuOGVtOw0KfQ0KLmNvbnRlbnRWaXMgeyAgICAJICAgICANCglmbG9hdDogbGVmdDsJDQoJbGluZS1oZWlnaHQ6IDI1cHg7DQoJbWFyZ2luOiAwcHg7DQoJcGFkZGluZzogMHB4Ow0KCXZlcnRpY2FsLWFsaWduOiB0b3A7DQogICAgZm9udC1zaXplOiAwLjc1ZW07CQkJDQp9DQoud29yZHdyYXAgeyANCiAgICB3aGl0ZS1zcGFjZTogcHJlLXdyYXA7ICAgICAgDQogICAgd2hpdGUtc3BhY2U6IC1tb3otcHJlLXdyYXA7IA0KICAgIHdoaXRlLXNwYWNlOiAtcHJlLXdyYXA7ICAgICANCiAgICB3aGl0ZS1zcGFjZTogLW8tcHJlLXdyYXA7ICAgDQogICAgd29yZC13cmFwOiBicmVhay13b3JkOyAgICAgIA0KfQkNCi51aS13aWRnZXQtY29udGVudCB7DQoJYmFja2dyb3VuZCA6IDUwJSA1MCUgcmVwZWF0LXggI2ZmZmZmZjsNCglib3JkZXIgOiAjZDRkNGQ0IHNvbGlkIDJweDsNCgljb2xvciA6ICM0ZjRlNGU7DQoJYm9yZGVyLXJhZGl1cyA6IDNweDsNCn0NCi51aS13aWRnZXQtaGVhZGVyIHsNCgljdXJzb3IgOiBwb2ludGVyOw0KCWZvbnQtc2l6ZSA6IDAuOGVtOw0KCWNvbG9yIDogIzQ5NDk0OTsNCglwYWRkaW5nLWxlZnQgOiAycHg7DQoJYm9yZGVyIDogI2VhZTllOCBzb2xpZCAxcHg7DQoJYmFja2dyb3VuZC1jb2xvciA6ICNlYWU5ZTg7DQoJbWFyZ2luLWJvdHRvbTogM3B4Ow0KCWJvcmRlci1yYWRpdXMgOiAzcHg7DQp9CQkJDQo8L3N0eWxlPg0KPC9oZWFkPg0KPGJvZHk+DQo8ZGl2IGlkPSJtYWluIiBjbGFzcz0ibGF5b3V0TWFpbiI+DQo8eHNsOmFwcGx5LXRlbXBsYXRlcy8+DQo8L2Rpdj4NCjwvYm9keT4NCjwvaHRtbD4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iL2Vnb25wOkdlbmVyYWxBZ2VuZGEiPg0KPGRpdiBjbGFzcz0ibGF5b3V0Um93IHVpLXRhYnMgdWktd2lkZ2V0LWNvbnRlbnQiID4NCjxkaXYgY2xhc3M9ImNhcHRpb24gdWktd2lkZ2V0LWhlYWRlciI+DQo8ZGl2IGNsYXNzPSJoZWFkZXJjb3JyZWN0aW9uIj5WxaFlb2JlY27DoSBhZ2VuZGE8L2Rpdj4NCjwvZGl2Pg0KPHhzbDphcHBseS10ZW1wbGF0ZXMgc2VsZWN0PSIuL2Vnb25wOnN1YmplY3QiLz4NCjx4c2w6YXBwbHktdGVtcGxhdGVzIHNlbGVjdD0iLi9lZ29ucDp0ZXh0Ii8+DQo8L2Rpdj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iZWdvbnA6R2VuZXJhbEFnZW5kYS9lZ29ucDpzdWJqZWN0Ij4NCjx4c2w6aWYgdGVzdD0iLi90ZXh0KCkiPg0KPGRpdj48bGFiZWwgY2xhc3M9ImxhYmVsVmlzIj5QcmVkbWV0OiA8L2xhYmVsPjxzcGFuIGNsYXNzPSJjb250ZW50VmlzIHdvcmR3cmFwIj48eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj48eHNsOndpdGgtcGFyYW0gbmFtZT0idGV4dCIgc2VsZWN0PSIuIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiclMEEnIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJieSIgc2VsZWN0PSInJiMxMzsmIzEwOyciIC8+PC94c2w6Y2FsbC10ZW1wbGF0ZT48L3NwYW4+PC9kaXY+PGRpdiBjbGFzcz0iY2xlYXIiPiYjeGEwOzwvZGl2PjwveHNsOmlmPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhL2Vnb25wOnRleHQiPg0KPHhzbDppZiB0ZXN0PSIuL3RleHQoKSI+DQo8ZGl2PjxsYWJlbCBjbGFzcz0ibGFiZWxWaXMiPlRleHQ6IDwvbGFiZWw+PHNwYW4gY2xhc3M9ImNvbnRlbnRWaXMgd29yZHdyYXAiPjx4c2w6Y2FsbC10ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9Ii4iIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJyUwQSciIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IicmIzEzOyYjMTA7JyIgLz48L3hzbDpjYWxsLXRlbXBsYXRlPjwvc3Bhbj48L2Rpdj48ZGl2IGNsYXNzPSJjbGVhciI+JiN4YTA7PC9kaXY+PC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VG9Ta0RhdGUiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkZGF0ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVN0cmluZyAhPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVN0cmluZyk9MTAgYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCAxLCA0KSkpICE9ICdOYU4nICI+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iY29uY2F0KHN1YnN0cmluZygkZGF0ZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVTdHJpbmcsIDEsIDQpKSIgLz4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkZGF0ZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iYm9vbGVhbkNoZWNrYm94VG9TdHJpbmciPg0KPHhzbDpwYXJhbSBuYW1lPSJib29sVmFsdWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9ImJvb2xWYWx1ZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGJvb2xWYWx1ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJ3RydWUnICI+DQo8eHNsOnRleHQ+w4FubzwveHNsOnRleHQ+DQo8L3hzbDp3aGVuPg0KPHhzbDp3aGVuIHRlc3Q9IiRib29sVmFsdWVTdHJpbmcgPSAnZmFsc2UnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICcxJyAiPg0KPHhzbDp0ZXh0PsOBbm88L3hzbDp0ZXh0Pg0KPC94c2w6d2hlbj4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJzAnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYm9vbFZhbHVlU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUaW1lVHJpbVNlY29uZHMiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0aW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJ0aW1lU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkdGltZSkiIC8+DQo8eHNsOmlmIHRlc3Q9IiR0aW1lU3RyaW5nICE9ICcnIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJzdWJzdHJpbmcoJHRpbWVTdHJpbmcsIDEsIDUpIiAvPg0KPC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9InRpbWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9InRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCR0aW1lKSIgLz4NCjx4c2w6aWYgdGVzdD0iJHRpbWVTdHJpbmcgIT0gJyciPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZygkdGltZVN0cmluZywgMSwgOCkiIC8+DQo8L3hzbDppZj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0ZXh0Ii8+DQo8eHNsOnBhcmFtIG5hbWU9InJlcGxhY2UiLz4NCjx4c2w6cGFyYW0gbmFtZT0iYnkiLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9ImNvbnRhaW5zKCR0ZXh0LCAkcmVwbGFjZSkiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZy1iZWZvcmUoJHRleHQsJHJlcGxhY2UpIi8+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGJ5Ii8+DQo8eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9InN1YnN0cmluZy1hZnRlcigkdGV4dCwkcmVwbGFjZSkiLz4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiRyZXBsYWNlIi8+DQo8eHNsOndpdGgtcGFyYW0gbmFtZT0iYnkiIHNlbGVjdD0iJGJ5IiAvPg0KPC94c2w6Y2FsbC10ZW1wbGF0ZT4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkdGV4dCIvPg0KPC94c2w6b3RoZXJ3aXNlPg0KPC94c2w6Y2hvb3NlPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRvU2tEYXRlVGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9ImRhdGVUaW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlVGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGRhdGVUaW1lKSIgLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9IiRkYXRlVGltZVN0cmluZyE9ICcnIGFuZCBzdHJpbmctbGVuZ3RoKCRkYXRlVGltZVN0cmluZyk+MTggYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCkpKSAhPSAnTmFOJyAiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9ImNvbmNhdChzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA5LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSwnICcsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEyLCAyKSwnOicsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDE1LCAyKSkiIC8+DQo8L3hzbDp3aGVuPg0KPHhzbDpvdGhlcndpc2U+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGRhdGVUaW1lU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUb1NrRGF0ZVRpbWVTZWNvbmQiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlVGltZSIgLz4NCjx4c2w6dmFyaWFibGUgbmFtZT0iZGF0ZVRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCRkYXRlVGltZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVRpbWVTdHJpbmchPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVRpbWVTdHJpbmcpPjE4IGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEsIDQpKSkgIT0gJ05hTicgIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJjb25jYXQoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgNiwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCksJyAnLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxMiwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxNSwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxOCwgMikpIiAvPg0KPC94c2w6d2hlbj4NCjx4c2w6b3RoZXJ3aXNlPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9IiRkYXRlVGltZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjwveHNsOnN0eWxlc2hlZXQ+DQoNCg==";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64InvalidXSLT() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var transformation = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjx4c2w6c3R5bGVzaGVldCB2ZXJzaW9uPSIxLjAiICB4bWxuczp4c2w9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvWFNML1RyYW5zZm9ybSIgIHhtbG5zOmVnb25wPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIGV4Y2x1ZGUtcmVzdWx0LXByZWZpeGVzPSJlZ29ucCI+DQo8eHNsOm91dHB1dCBtZXRob2Q9Imh0bWwiIGRvY3R5cGUtc3lzdGVtPSJodHRwOi8vd3d3LnczLm9yZy9UUi9odG1sNC9zdHJpY3QuZHRkIiBkb2N0eXBlLXB1YmxpYz0iLS8vVzNDLy9EVEQgSFRNTCA0LjAxLy9FTiIgaW5kZW50PSJubyIgb21pdC14bWwtZGVjbGFyYXRpb249InllcyIvPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iLyI+DQo8aHRtbD4NCjxoZWFkPg0KPG1ldGEgaHR0cC1lcXVpdj0iWC1VQS1Db21wYXRpYmxlIiBjb250ZW50PSJJRT04IiAvPg0KPHRpdGxlPlbFoWVvYmVjbsOhIGFnZW5kYTwvdGl0bGU+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD1VVEYtOCIvPg0KPG1ldGEgbmFtZT0ibGFuZ3VhZ2UiIGNvbnRlbnQ9InNrLVNLIi8+DQo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPg0KYm9keSB7IA0KCWZvbnQtZmFtaWx5OiAnT3BlbiBTYW5zJywgJ1NlZ29lIFVJJywgJ1RyZWJ1Y2hldCBNUycsICdHZW5ldmEgQ0UnLCBsdWNpZGEsIHNhbnMtc2VyaWY7DQoJYmFja2dyb3VuZCA6ICNmZmZmZmYgIWltcG9ydGFudCA7DQp9DQoudWktdGFicyB7DQoJcGFkZGluZzogLjJlbTsNCglwb3NpdGlvbjogcmVsYXRpdmU7DQoJem9vbTogMTsNCn0JCQkJCQkJCQ0KLmNsZWFyIHsgY2xlYXI6IGJvdGg7IGhlaWdodDogMDt9DQoubGF5b3V0TWFpbiB7DQoJbWFyZ2luOiAwcHggYXV0bzsNCglwYWRkaW5nOiA1cHggNXB4IDVweCA1cHg7CQ0KfQkJCQkNCi5sYXlvdXRSb3cgeyBtYXJnaW4tYm90dG9tOiA1cHg7IH0JCQkJDQouY2FwdGlvbiB7IC8qd2lkdGg6IDEwMCU7IGJvcmRlci1ib3R0b206IHNvbGlkIDFweCBibGFjazsqLyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24geyBib3JkZXI6IDBweCAhaW1wb3J0YW50OyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24gc3BhbiB7DQoJYmFja2dyb3VuZDogbm9uZSAhaW1wb3J0YW50Ow0KCWRpc3BsYXk6IG5vbmU7DQp9IA0KLmNhcHRpb24gLnRpdGxlIHsgcGFkZGluZy1sZWZ0OiA1cHg7IH0NCi5oZWFkZXJjb3JyZWN0aW9uIHsJDQoJbWFyZ2luOiAwcHg7DQogICAgZm9udC1zaXplIDogMWVtOw0KICAgIGZvbnQtd2VpZ2h0OiBib2xkOw0KfQkJCQkNCi5sYWJlbFZpcyB7DQoJZmxvYXQ6IGxlZnQ7DQoJZm9udC13ZWlnaHQ6IGJvbGQ7DQoJZm9udC1mYW1pbHk6ICdPcGVuIFNhbnMnLCAnU2Vnb2UgVUknLCAnVHJlYnVjaGV0IE1TJywgJ0dlbmV2YSBDRScsIGx1Y2lkYSwgc2Fucy1zZXJpZjsNCglsaW5lLWhlaWdodDogMjVweDsNCgltYXJnaW46IDBweCAxOHB4IDBweCAwcHg7DQoJcGFkZGluZy1sZWZ0OiAzcHg7DQoJd2lkdGg6IDE5MHB4Ow0KCXdvcmQtd3JhcDogYnJlYWstd29yZDsNCiAgICBmb250LXNpemU6IDAuOGVtOw0KfQ0KLmNvbnRlbnRWaXMgeyAgICAJICAgICANCglmbG9hdDogbGVmdDsJDQoJbGluZS1oZWlnaHQ6IDI1cHg7DQoJbWFyZ2luOiAwcHg7DQoJcGFkZGluZzogMHB4Ow0KCXZlcnRpY2FsLWFsaWduOiB0b3A7DQogICAgZm9udC1zaXplOiAwLjc1ZW07CQkJDQp9DQoud29yZHdyYXAgeyANCiAgICB3aGl0ZS1zcGFjZTogcHJlLXdyYXA7ICAgICAgDQogICAgd2hpdGUtc3BhY2U6IC1tb3otcHJlLXdyYXA7IA0KICAgIHdoaXRlLXNwYWNlOiAtcHJlLXdyYXA7ICAgICANCiAgICB3aGl0ZS1zcGFjZTogLW8tcHJlLXdyYXA7ICAgDQogICAgd29yZC13cmFwOiBicmVhay13b3JkOyAgICAgIA0KfQkNCi51aS13aWRnZXQtY29udGVudCB7DQoJYmFja2dyb3VuZCA6IDUwJSA1MCUgcmVwZWF0LXggI2ZmZmZmZjsNCglib3JkZXIgOiAjZDRkNGQ0IHNvbGlkIDJweDsNCgljb2xvciA6ICM0ZjRlNGU7DQoJYm9yZGVyLXJhZGl1cyA6IDNweDsNCn0NCi51aS13aWRnZXQtaGVhZGVyIHsNCgljdXJzb3IgOiBwb2ludGVyOw0KCWZvbnQtc2l6ZSA6IDAuOGVtOw0KCWNvbG9yIDogIzQ5NDk0OTsNCglwYWRkaW5nLWxlZnQgOiAycHg7DQoJYm9yZGVyIDogI2VhZTllOCBzb2xpZCAxcHg7DQoJYmFja2dyb3VuZC1jb2xvciA6ICNlYWU5ZTg7DQoJbWFyZ2luLWJvdHRvbTogM3B4Ow0KCWJvcmRlci1yYWRpdXMgOiAzcHg7DQp9CQkJDQo8L3N0eWxlPg0KPC9oZWFkPg0KPGJvZHk+DQo8ZGl2IGlkPSJtYWluIiBjbGFzcz0ibGF5b3V0TWFpbiI+DQo8eHNsOmFwcGx5LXRlbXBsYXRlcy8+DQo8L2Rpdj4NCjwvYm9keT4NCjwvaHRtbD4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iL2Vnb25wOkdlbmVyYWxBZ2VuZGEiPg0KPGRpdiBjbGFzcz0ibGF5b3V0Um93IHVpLXRhYnMgdWktd2lkZ2V0LWNvbnRlbnQiID4NCjxkaXYgY2xhc3M9ImNhcHRpb24gdWktd2lkZ2V0LWhlYWRlciI+DQo8ZGl2IGNsYXNzPSJoZWFkZXJjb3JyZWN0aW9uIj5WxaFlb2JlY27DoSBhZ2VuZGE8L2Rpdj4NCjwvZGl2Pg0KPHhzbDphcHBseS10ZW1wbGF0ZXMgc2VsZWN0PSIuL2Vnb25wOnN1YmplY3QiLz4NCjx4c2w6YXBwbHktdGVtcGxhdGVzIHNlbGVjdD0iLi9lZ29ucDp0ZXh0Ii8+DQo8L2Rpdj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iZWdvbnA6R2VuZXJhbEFnZW5kYS9lZ29ucDpzdWJqZWN0Ij4NCjx4c2w6aWYgdGVzdD0iLi90ZXh0KCkiPg0KPGRpdj48bGFiZWwgY2xhc3M9ImxhYmVsVmlzIj5QcmVkbWV0OiA8L2xhYmVsPjxzcGFuIGNsYXNzPSJjb250ZW50VmlzIHdvcmR3cmFwIj48eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj48eHNsOndpdGgtcGFyYW0gbmFtZT0idGV4dCIgc2VsZWN0PSIuIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiclMEEnIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJieSIgc2VsZWN0PSInJiMxMzsmIzEwOyciIC8+PC94c2w6Y2FsbC10ZW1wbGF0ZT48L3NwYW4+PC9kaXY+PGRpdiBjbGFzcz0iY2xlYXIiPiYjeGEwOzwvZGl2PjwveHNsOmlmPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhL2Vnb25wOnRleHQiPg0KPHhzbDppZiB0ZXN0PSIuL3RleHQoKSI+DQo8ZGl2PjxsYWJlbCBjbGFzcz0ibGFiZWxWaXMiPlRleHQ6IDwvbGFiZWw+PHNwYW4gY2xhc3M9ImNvbnRlbnRWaXMgd29yZHdyYXAiPjx4c2w6Y2FsbC10ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9Ii4iIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJyUwQSciIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IicmIzEzOyYjMTA7JyIgLz48L3hzbDpjYWxsLXRlbXBsYXRlPjwvc3Bhbj48L2Rpdj48ZGl2IGNsYXNzPSJjbGVhciI+JiN4YTA7PC9kaXY+PC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VG9Ta0RhdGUiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkZGF0ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVN0cmluZyAhPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVN0cmluZyk9MTAgYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCAxLCA0KSkpICE9ICdOYU4nICI+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iY29uY2F0KHN1YnN0cmluZygkZGF0ZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVTdHJpbmcsIDEsIDQpKSIgLz4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkZGF0ZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iYm9vbGVhbkNoZWNrYm94VG9TdHJpbmciPg0KPHhzbDpwYXJhbSBuYW1lPSJib29sVmFsdWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9ImJvb2xWYWx1ZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGJvb2xWYWx1ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJ3RydWUnICI+DQo8eHNsOnRleHQ+w4FubzwveHNsOnRleHQ+DQo8L3hzbDp3aGVuPg0KPHhzbDp3aGVuIHRlc3Q9IiRib29sVmFsdWVTdHJpbmcgPSAnZmFsc2UnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICcxJyAiPg0KPHhzbDp0ZXh0PsOBbm88L3hzbDp0ZXh0Pg0KPC94c2w6d2hlbj4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJzAnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYm9vbFZhbHVlU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUaW1lVHJpbVNlY29uZHMiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0aW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJ0aW1lU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkdGltZSkiIC8+DQo8eHNsOmlmIHRlc3Q9IiR0aW1lU3RyaW5nICE9ICcnIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJzdWJzdHJpbmcoJHRpbWVTdHJpbmcsIDEsIDUpIiAvPg0KPC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9InRpbWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9InRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCR0aW1lKSIgLz4NCjx4c2w6aWYgdGVzdD0iJHRpbWVTdHJpbmcgIT0gJyciPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZygkdGltZVN0cmluZywgMSwgOCkiIC8+DQo8L3hzbDppZj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0ZXh0Ii8+DQo8eHNsOnBhcmFtIG5hbWU9InJlcGxhY2UiLz4NCjx4c2w6cGFyYW0gbmFtZT0iYnkiLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9ImNvbnRhaW5zKCR0ZXh0LCAkcmVwbGFjZSkiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZy1iZWZvcmUoJHRleHQsJHJlcGxhY2UpIi8+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGJ5Ii8+DQo8eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9InN1YnN0cmluZy1hZnRlcigkdGV4dCwkcmVwbGFjZSkiLz4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiRyZXBsYWNlIi8+DQo8eHNsOndpdGgtcGFyYW0gbmFtZT0iYnkiIHNlbGVjdD0iJGJ5IiAvPg0KPC94c2w6Y2FsbC10ZW1wbGF0ZT4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkdGV4dCIvPg0KPC94c2w6b3RoZXJ3aXNlPg0KPC94c2w6Y2hvb3NlPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRvU2tEYXRlVGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9ImRhdGVUaW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlVGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGRhdGVUaW1lKSIgLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9IiRkYXRlVGltZVN0cmluZyE9ICcnIGFuZCBzdHJpbmctbGVuZ3RoKCRkYXRlVGltZVN0cmluZyk+MTggYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCkpKSAhPSAnTmFOJyAiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9ImNvbmNhdChzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA5LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSwnICcsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEyLCAyKSwnOicsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDE1LCAyKSkiIC8+DQo8L3hzbDp3aGVuPg0KPHhzbDpvdGhlcndpc2U+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGRhdGVUaW1lU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUb1NrRGF0ZVRpbWVTZWNvbmQiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlVGltZSIgLz4NCjx4c2w6dmFyaWFibGUgbmFtZT0iZGF0ZVRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCRkYXRlVGltZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVRpbWVTdHJpbmchPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVRpbWVTdHJpbmcpPjE4IGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEsIDQpKSkgIT0gJ05hTicgIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJjb25jYXQoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgNiwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCksJyAnLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxMiwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxNSwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxOCwgMikpIiAvPg0KPC94c2w6d2hlbj4NCjx4c2w6b3RoZXJ3aXNlPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9IiRkYXRlVGltZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjwveHNsOnN0eWxlc2hlZXQ+DQoNCg==";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64EmptyXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64EmptyXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var schema = "";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64EmptyXSLT() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "";
        var transformation = "";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(TransformationParsingErrorException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64EmptyXSLT() {
        var xmlContent = "UEsDBAoAAAgAALNOEFeKIflFHwAAAB8AAAAIAAAAbWltZXR5cGVhcHBsaWNhdGlvbi92bmQuZXRzaS5hc2ljLWUremlwUEsDBBQACAgIALNOEFcAAAAAAAAAAAAAAAAMAAAAZG9jdW1lbnQueG1sxZTbctowEIZfRaPbDJIVSAoUk+GUtE1oKYEELhVrMQpGciVxSN+ml32OvFhlHCB0aGY6veildn+t/v127drFep6gJRgrtQoxIwFGoCItpIpDPBxcFsr4ol5bi6g66t60ueMtrRyXCgzyN5Wt+lSIp86lVUqFz5NYL4mdUQETGm211Guz5C5w4gOUEYYPaqOsOCg3eEohxDxNExlx551l99+jaMqNBRfmttBH4aVyIsEcN6AjChNt5rSRpuQK/Ks8acSgBPcvVzC623dd8T4OFHlzu7o2msKc223pt6q+ULFyd3m1WpFVkWgT09MgYNQ3e7upV5DKOq4i8K/bxcMjRK7+WS+ff6JUC64k1Og2XHOwdvWeFs8/lnyOnHYaqddSUqMbSY0eePLnV3xz2EMLIjdg+zAB4+cNYp8a3bZ3YdSWMVjXBTfVIsQLo6paiuopYeekXAqIn2DASJGUiA/hF/UdTxZ+fLTlVNCstN99uFbnw/5lvyx7w+vJ0xWU5s1xen1yeiYiJsf3i06I0cBwZTOujSTWRrrp/Bi/QT9H2O+0Ch50IWIlVcgiQZGd4frfTmuTImsrckq/d79n0jNg/bJtdjFHt0f05521icv2HP8Lxa+aPY5H7NP9k1h26ZcbapQ06XTGTJd9L7FJrz9qduKUfnvw3yu64Spe8NjfszOMuiAkb/taUm0cZRb9MTIydZvFH4wG/5l84vbo34Bc36uObC499nuq/wJQSwcImbqZaCoCAADbBAAAUEsDBBQACAgIALNOEFcAAAAAAAAAAAAAAAAaAAAATUVUQS1JTkYvc2lnbmF0dXJlczAwMS54bWy1V9l26koO/ZUs8kgnHhmcleQuzxhsjCew/eahPIAnXAYDX38NnJDh5HbnrO5+wqWSVNoqlbR5/uuQZ3d7UMO0LF562CPauwNFUIZpEb/0LFN4GPf+en32YBo82XTIG0YaF16zqwG86ywL+HTeeuklTVM9IciuTh9BA9PHso4RFKewMbLHHvFH7L73+hzCp5v1L+MQ3kzbtn1siYshjqIoglJIpxPCNL7v3UnhSy8NH3xAYhQ59DycxDwyDMMhPgTeiBhhRBRiEfp+CAilIiovS9YryiINvCw9eU2HUgFNUoZ3dBaXddok+XcRmPo5CAzRefahi+IhwMji4SxBCWzQQz5j+YnDizeUfIP0kJc1uK+h9wATDx8Mf7nUQQTqLv3gArh++AHkB6x3Z+nSSy8sg10OiuaxO+KaBy6NAWz+MLru9PtPMV29LL1sB153DqmG26PQkDMgMC5Sa1a2CtTRasMzTGGUKe5sUUvGMdR6eUa+GF8EN4Rf8JrHCvxDFWEUStxf73RRlxWomxTAX6DvD14I4E/ydE2JWXsFjMo6h5+X/2U1IL+7/t8nX2lopjL37G6+4/soFSkNzkWur8nY8LgQCmGWVfupmB2Sbfyfk4/8/k5u9XyxuFTg/vz1s+zWntqs10m8gLKxngyKiJR0MzJJMzzNSFcdpWXA7YcHZBl7nJ7R9oRPUBAsJstmmfoTa1O54haXOTfzLZjCeUFrLKmdGDtaWNV0RVaoOSdSW8fdui+rK1EQj4OVXoAkqlWhv4H1YsqZJ7tv5IFaVAjFMXbTqlm+7mPTATwUFiyxXMYih8COAmPaLYPT2GqmWeSmX5PDgF3KVX8uOy4hejsfDruoa6x0UAHiKEX5Gb5NJ6aEZ1LhYLEwBLiYiCNCl711M4YzQ9hIwFkCdjeO29CooDnDJptxH1uNCoUNapDLoi4fuDTn4l22Kfkhx4x5ZaAwI3NAi9l2m0YrYqyZzTRrF2QEIcc3FTuLX15uN/V+NZfbmoHj7ebsAUpxXuPdFsbOX4OgmXs5eGXnL9OuA94ZuyDxsn+pL0aZdf3+TjIvrr+qv7lgz+8s6tpm01WeJLEox7K0n8Z0KzF0LPEOgriQo+dMvNkmm1SkWpShNSjQHL1VdKnlaYdbatqMozPTxw+4a0uxsdQOAkcbTDxfMrSisGiV+Pk4tojl2hMFqDCkzZnSSeGUVrn+HpSsPMtQhZOO6rusna15Q2FokcYsno3bqYVTMMyXR4nPLEXXWj6+nM9xdDNzVoO9JMwxB08S/4zDkFpOc6az0pWSfTCnta59aTQXx/yCPu9rJdt9M7QcasVQ1Ed76Ujac/qEGMxCW6+zfWBOh+pWYcnM4HRg0PspzlMqVm0GAnTcMB+FrKyiaGysHDv2I0VBbZKoA+jssBjfbx2SSilga8lRmW1OIkP5mqri7MLWBpIkQKQwRE7sr9Mmd/wWIqWLjHUu1gIzD9ZkbWBmSVFLoLVOVHt75mj746F1IiQrq4eRfzyGzMQaJuuD5yL93dTLTgRpCk5FHYj+wZ1wDlBd9TBJElTOI3TMi0i+DXcJK49jWaU4Itv7toGKA8kmJNmo8KlrGJtyq8JCmtpj9rCQZ7Fl7IHoJ+uSiFaZ4yKkqR1U2hF0ns/N/LRD1FM1tIX5mKd1sisvVEMRapNS7VS1WLqrDtr8pnZotcs9TwcjWS58Z0XkkzXeNxdSB45DzPESsZcrzZu2VHFgC3c+PQSetKRISmrDIScPwI7jJ4UplGaJrMcGDeZ5sS6KMsodMNMwaTndblmbRJgRxfHQpof41MqVhkgq0fKMfW0fTYkqp7akb7eWCAe0HlXQx3bhWJdDdwEhitSnlVL041wngVVM/W07IbDqMO1jxGIM9UUhTJXIIX0pzjfHIz1HOUjWS+qI7rVsJSCjDZlM55CJ3fqkdlXjOiNG9VuuL2BG53Pf4stcQHSyzviMAOMDS+qJPjBOXVNYCCEaK9meJpZcEInaoC28tW7pm4FHJvyBDVTSphV6lmxELCAPq20GEC9cTKo9GQZjRqG1X73k6+O+Ca8dBPnSW9RLa3h9vgzaJ23X0ajo2JHD91H8i8pdFP7NCD8TQeIR78ic6dUxaLrh/bOxfT35KwG4zKc/mf4f3dz66bu/jwodPDPteiGO4sQDOn7AhiY6ehoQT8TYfUZ+1/ts+im5152z6OP3dTL/n3iCf9znC0hlidyAIpW2/hGRETdPj5ZwqhZ1mBxRy5zDcnFov+MJv0d5lUgQ7kBtgDr1stuUuAp/Omc+aN8m1cXffJf7oH7FhqPRCMNGFP4+mj7uvwX3ORTkY46Rf74K5A8qAITn13Ct/d813veEju95zd11caNXXWn/lLu/uVS6Ojpz4FevqrJzwN0fFWRfhI9xuX+EmzOpD7tTg7JovLQAdb8TvCG62b4Jvsb3Bfv30JDv39lt47unf20Xby3iE1Pplt/9aXz9G1BLBwipbHfAPQcAAHEOAABQSwMEFAAICAgAs04QVwAAAAAAAAAAAAAAABUAAABNRVRBLUlORi9tYW5pZmVzdC54bWyNkMFOAzEMRH9l5WuVLHBCUdPe+AL4ACvxFovEiTbeFeXrSZFaFnHpzSPNzBt5f/zMaVhpblzEw6N9gIEklMhy8vD2+mKe4XjYZxSeqKm7HkOPSbtJD8ssrmDj5gQzNafBlUoSS1gyibq/fvcDuqkN/wk2tIkTmZ6ez7/eaUnJVNR3D+OmIlNkNHqu5AFrTRxQe+W4SrSkjW3fFgztvrjCeD/iut/2/XfSTmW17eMSiKgYiiiy0Ly7NHTy+O+Xh29QSwcIuT2EmMAAAACFAQAAUEsBAgoACgAACAAAs04QV4oh+UUfAAAAHwAAAAgAAAAAAAAAAAAAAAAAAAAAAG1pbWV0eXBlUEsBAhQAFAAICAgAs04QV5m6mWgqAgAA2wQAAAwAAAAAAAAAAAAAAAAARQAAAGRvY3VtZW50LnhtbFBLAQIUABQACAgIALNOEFepbHfAPQcAAHEOAAAaAAAAAAAAAAAAAAAAAKkCAABNRVRBLUlORi9zaWduYXR1cmVzMDAxLnhtbFBLAQIUABQACAgIALNOEFe5PYSYwAAAAIUBAAAVAAAAAAAAAAAAAAAAAC4KAABNRVRBLUlORi9tYW5pZmVzdC54bWxQSwUGAAAAAAQABAD7AAAAMQsAAAAA";
        var document = new Document(xmlContent);

        var transformation = "";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64DigestMismatchXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQxMTExMVVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64DigestMismatchXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9InByaWV6dmlza28iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                schema,
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64DigestMismatchXSLT() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var transformation = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjx4c2w6c3R5bGVzaGVldCB2ZXJzaW9uPSIxLjAiICB4bWxuczp4c2w9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvWFNML1RyYW5zZm9ybSIgIHhtbG5zOmVnb25wPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIGV4Y2x1ZGUtcmVzdWx0LXByZWZpeGVzPSJlZ29ucCI+DQo8eHNsOm91dHB1dCBtZXRob2Q9Imh0bWwiIGRvY3R5cGUtc3lzdGVtPSJodHRwOi8vd3d3LnczLm9yZy9UUi9odG1sNC9zdHJpY3QuZHRkIiBkb2N0eXBlLXB1YmxpYz0iLS8vVzNDLy9EVEQgSFRNTCA0LjAxLy9FTiIgaW5kZW50PSJubyIgb21pdC14bWwtZGVjbGFyYXRpb249InllcyIvPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iLyI+DQo8aHRtbD4NCjxoZWFkPg0KPG1ldGEgaHR0cC1lcXVpdj0iWC1VQS1Db21wYXRpYmxlIiBjb250ZW50PSJJRT04IiAvPg0KPHRpdGxlPlbFoWVvYmVjbsOhIGFnZW5kYTwvdGl0bGU+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD1VVEYtOCIvPg0KPG1ldGEgbmFtZT0ibGFuZ3VhZ2UiIGNvbnRlbnQ9InNrLVNLIi8+DQo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPg0KYm9keSB7IA0KCWZvbnQtZmFtaWx5OiAnT3BlbiBTYW5zJywgJ1NlZ29lIFVJJywgJ1RyZWJ1Y2hldCBNUycsICdHZW5ldmEgQ0UnLCBsdWNpZGEsIHNhbnMtc2VyaWY7DQoJYmFja2dyb3VuZCA6ICNmZmZmZmYgIWltcG9ydGFudCA7DQp9DQoudWktdGFicyB7DQoJcGFkZGluZzogLjJlbTsNCglwb3NpdGlvbjogcmVsYXRpdmU7DQoJem9vbTogMTsNCn0JCQkJCQkJCQ0KLmNsZWFyIHsgY2xlYXI6IGJvdGg7IGhlaWdodDogMDt9DQoubGF5b3V0TWFpbiB7DQoJbWFyZ2luOiAwcHggYXV0bzsNCglwYWRkaW5nOiA1cHggNXB4IDVweCA1cHg7CQ0KfQkJCQkNCi5sYXlvdXRSb3cgeyBtYXJnaW4tYm90dG9tOiA1cHg7IH0JCQkJDQouY2FwdGlvbiB7IC8qd2lkdGg6IDEwMCU7IGJvcmRlci1ib3R0b206IHNvbGlkIDFweCBibGFjazsqLyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24geyBib3JkZXI6IDBweCAhaW1wb3J0YW50OyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24gc3BhbiB7DQoJYmFja2dyb3VuZDogbm9uZSAhaW1wb3J0YW50Ow0KCWRpc3BsYXk6IG5vbmU7DQp9IA0KLmNhcHRpb24gLnRpdGxlIHsgcGFkZGluZy1sZWZ0OiA1cHg7IH0NCi5oZWFkZXJjb3JyZWN0aW9uIHsJDQoJbWFyZ2luOiAwcHg7DQogICAgZm9udC1zaXplIDogMWVtOw0KICAgIGZvbnQtd2VpZ2h0OiBib2xkOw0KfQkJCQkNCi5sYWJlbFZpcyB7DQoJZmxvYXQ6IGxlZnQ7DQoJZm9udC13ZWlnaHQ6IGJvbGQ7DQoJZm9udC1mYW1pbHk6ICdPcGVuIFNhbnMnLCAnU2Vnb2UgVUknLCAnVHJlYnVjaGV0IE1TJywgJ0dlbmV2YSBDRScsIGx1Y2lkYSwgc2Fucy1zZXJpZjsNCglsaW5lLWhlaWdodDogMjVweDsNCgltYXJnaW46IDBweCAxOHB4IDBweCAwcHg7DQoJcGFkZGluZy1sZWZ0OiAzcHg7DQoJd2lkdGg6IDE5MHB4Ow0KCXdvcmQtd3JhcDogYnJlYWstd29yZDsNCiAgICBmb250LXNpemU6IDAuOGVtOw0KfQ0KLmNvbnRlbnRWaXMgeyAgICAJICAgICANCglmbG9hdDogbGVmdDsJDQoJbGluZS1oZWlnaHQ6IDI1cHg7DQoJbWFyZ2luOiAwcHg7DQoJcGFkZGluZzogMHB4Ow0KCXZlcnRpY2FsLWFsaWduOiB0b3A7DQogICAgZm9udC1zaXplOiAwLjc1ZW07CQkJDQp9DQoud29yZHdyYXAgeyANCiAgICB3aGl0ZS1zcGFjZTogcHJlLXdyYXA7ICAgICAgDQogICAgd2hpdGUtc3BhY2U6IC1tb3otcHJlLXdyYXA7IA0KICAgIHdoaXRlLXNwYWNlOiAtcHJlLXdyYXA7ICAgICANCiAgICB3aGl0ZS1zcGFjZTogLW8tcHJlLXdyYXA7ICAgDQogICAgd29yZC13cmFwOiBicmVhay13b3JkOyAgICAgIA0KfQkNCi51aS13aWRnZXQtY29udGVudCB7DQoJYmFja2dyb3VuZCA6IDUwJSA1MCUgcmVwZWF0LXggI2ZmZmZmZjsNCglib3JkZXIgOiAjZDRkNGQ0IHNvbGlkIDJweDsNCgljb2xvciA6ICM0ZjRlNGU7DQoJYm9yZGVyLXJhZGl1cyA6IDNweDsNCn0NCi51aS13aWRnZXQtaGVhZGVyIHsNCgljdXJzb3IgOiBwb2ludGVyOw0KCWZvbnQtc2l6ZSA6IDAuOGVtOw0KCWNvbG9yIDogIzQ5NDk0OTsNCglwYWRkaW5nLWxlZnQgOiAycHg7DQoJYm9yZGVyIDogI2VhZTllOCBzb2xpZCAxcHg7DQoJYmFja2dyb3VuZC1jb2xvciA6ICNlYWU5ZTg7DQoJbWFyZ2luLWJvdHRvbTogM3B4Ow0KCWJvcmRlci1yYWRpdXMgOiAzcHg7DQp9CQkJDQo8L3N0eWxlPg0KPC9oZWFkPg0KPGJvZHk+DQo8ZGl2IGlkPSJtYWluIiBjbGFzcz0ibGF5b3V0TWFpbiI+DQo8eHNsOmFwcGx5LXRlbXBsYXRlcy8+DQo8L2Rpdj4NCjwvYm9keT4NCjwvaHRtbD4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iL2Vnb25wOkdlbmVyYWxBZ2VuZGEiPg0KPGRpdiBjbGFzcz0ibGF5b3V0Um93IHVpLXRhYnMgdWktd2lkZ2V0LWNvbnRlbnQiID4NCjxkaXYgY2xhc3M9ImNhcHRpb24gdWktd2lkZ2V0LWhlYWRlciI+DQo8ZGl2IGNsYXNzPSJoZWFkZXJjb3JyZWN0aW9uIj5WxaFlb2JlY27DoSBhZ2VuZGE8L2Rpdj4NCjwvZGl2Pg0KPHhzbDphcHBseS10ZW1wbGF0ZXMgc2VsZWN0PSIuL2Vnb25wOnN1YmplY3QiLz4NCjx4c2w6YXBwbHktdGVtcGxhdGVzIHNlbGVjdD0iLi9lZ29ucDp0ZXh0Ii8+DQo8L2Rpdj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iZWdvbnA6R2VuZXJhbEFnZW5kYS9lZ29ucDpzdWJqZWN0Ij4NCjx4c2w6aWYgdGVzdD0iLi90ZXh0KCkiPg0KPGRpdj48bGFiZWwgY2xhc3M9ImxhYmVsVmlzIj5QcmVkbWV0OiA8L2xhYmVsPjxzcGFuIGNsYXNzPSJjb250ZW50VmlzIHdvcmR3cmFwIj48eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj48eHNsOndpdGgtcGFyYW0gbmFtZT0idGV4dCIgc2VsZWN0PSIuIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiclMEEnIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJieSIgc2VsZWN0PSInJiMxMzsmIzEwOyciIC8+PC94c2w6Y2FsbC10ZW1wbGF0ZT48L3NwYW4+PC9kaXY+PGRpdiBjbGFzcz0iY2xlYXIiPiYjeGEwOzwvZGl2PjwveHNsOmlmPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhL2Vnb25wOnRleHQiPg0KPHhzbDppZiB0ZXN0PSIuL3RleHQoKSI+DQo8ZGl2PjxsYWJlbCBjbGFzcz0ibGFiZWxWaXMiPlRleHQ6IDwvbGFiZWw+PHNwYW4gY2xhc3M9ImNvbnRlbnRWaXMgd29yZHdyYXAiPjx4c2w6Y2FsbC10ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9Ii4iIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJyUwQSciIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IicmIzEzOyYjMTA7JyIgLz48L3hzbDpjYWxsLXRlbXBsYXRlPjwvc3Bhbj48L2Rpdj48ZGl2IGNsYXNzPSJjbGVhciI+JiN4YTA7PC9kaXY+PC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VG9Ta0RhdGUiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkZGF0ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVN0cmluZyAhPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVN0cmluZyk9MTAgYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCAxLCA0KSkpICE9ICdOYU4nICI+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iY29uY2F0KHN1YnN0cmluZygkZGF0ZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVTdHJpbmcsIDEsIDQpKSIgLz4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkZGF0ZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iYm9vbGVhbkNoZWNrYm94VG9TdHJpbmciPg0KPHhzbDpwYXJhbSBuYW1lPSJib29sVmFsdWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9ImJvb2xWYWx1ZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGJvb2xWYWx1ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJ3RydWUnICI+DQo8eHNsOnRleHQ+w4FubzwveHNsOnRleHQ+DQo8L3hzbDp3aGVuPg0KPHhzbDp3aGVuIHRlc3Q9IiRib29sVmFsdWVTdHJpbmcgPSAnZmFsc2UnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICcxJyAiPg0KPHhzbDp0ZXh0PsOBbm88L3hzbDp0ZXh0Pg0KPC94c2w6d2hlbj4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJzAnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYm9vbFZhbHVlU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUaW1lVHJpbVNlY29uZHMiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0aW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJ0aW1lU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkdGltZSkiIC8+DQo8eHNsOmlmIHRlc3Q9IiR0aW1lU3RyaW5nICE9ICcnIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJzdWJzdHJpbmcoJHRpbWVTdHJpbmcsIDEsIDUpIiAvPg0KPC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9InRpbWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9InRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCR0aW1lKSIgLz4NCjx4c2w6aWYgdGVzdD0iJHRpbWVTdHJpbmcgIT0gJyciPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZygkdGltZVN0cmluZywgMSwgOCkiIC8+DQo8L3hzbDppZj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0ZXh0Ii8+DQo8eHNsOnBhcmFtIG5hbWU9InJlcGxhY2UiLz4NCjx4c2w6cGFyYW0gbmFtZT0iYnkiLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9ImNvbnRhaW5zKCR0ZXh0LCAkcmVwbGFjZSkiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZy1iZWZvcmUoJHRleHQsJHJlcGxhY2UpIi8+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGJ5Ii8+DQo8eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9InN1YnN0cmluZy1hZnRlcigkdGV4dCwkcmVwbGFjZSkiLz4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiRyZXBsYWNlIi8+DQo8eHNsOndpdGgtcGFyYW0gbmFtZT0iYnkiIHNlbGVjdD0iJGJ5IiAvPg0KPC94c2w6Y2FsbC10ZW1wbGF0ZT4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkdGV4dCIvPg0KPC94c2w6b3RoZXJ3aXNlPg0KPC94c2w6Y2hvb3NlPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRvU2tEYXRlVGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9ImRhdGVUaW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlVGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGRhdGVUaW1lKSIgLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9IiRkYXRlVGltZVN0cmluZyE9ICcnIGFuZCBzdHJpbmctbGVuZ3RoKCRkYXRlVGltZVN0cmluZyk+MTggYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCkpKSAhPSAnTmFOJyAiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9ImNvbmNhdChzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA5LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSwnICcsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEyLCAyKSwnOicsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDE1LCAyKSkiIC8+DQo8L3hzbDp3aGVuPg0KPHhzbDpvdGhlcndpc2U+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGRhdGVUaW1lU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUb1NrRGF0ZVRpbWVTZWNvbmQiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlVGltZSIgLz4NCjx4c2w6dmFyaWFibGUgbmFtZT0iZGF0ZVRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCRkYXRlVGltZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVRpbWVTdHJpbmchPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVRpbWVTdHJpbmcpPjE4IGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEsIDQpKSkgIT0gJ05hTicgIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJjb25jYXQoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgNiwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCksJyAnLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxMiwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxNSwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxOCwgMikpIiAvPg0KPC94c2w6d2hlbj4NCjx4c2w6b3RoZXJ3aXNlPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9IiRkYXRlVGltZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjwveHNsOnN0eWxlc2hlZXQ+DQoNCg==";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64DigestMismatchXSLT() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var transformation = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjx4c2w6c3R5bGVzaGVldCB2ZXJzaW9uPSIxLjAiICB4bWxuczp4c2w9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvWFNML1RyYW5zZm9ybSIgIHhtbG5zOmVnb25wPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIGV4Y2x1ZGUtcmVzdWx0LXByZWZpeGVzPSJlZ29ucCI+DQo8eHNsOm91dHB1dCBtZXRob2Q9Imh0bWwiIGRvY3R5cGUtc3lzdGVtPSJodHRwOi8vd3d3LnczLm9yZy9UUi9odG1sNC9zdHJpY3QuZHRkIiBkb2N0eXBlLXB1YmxpYz0iLS8vVzNDLy9EVEQgSFRNTCA0LjAxLy9FTiIgaW5kZW50PSJubyIgb21pdC14bWwtZGVjbGFyYXRpb249InllcyIvPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iLyI+DQo8aHRtbD4NCjxoZWFkPg0KPG1ldGEgaHR0cC1lcXVpdj0iWC1VQS1Db21wYXRpYmxlIiBjb250ZW50PSJJRT04IiAvPg0KPHRpdGxlPlbFoWVvYmVjbsOhIGFnZW5kYTwvdGl0bGU+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD1VVEYtOCIvPg0KPG1ldGEgbmFtZT0ibGFuZ3VhZ2UiIGNvbnRlbnQ9InNrLVNLIi8+DQo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPg0KYm9keSB7IA0KCWZvbnQtZmFtaWx5OiAnT3BlbiBTYW5zJywgJ1NlZ29lIFVJJywgJ1RyZWJ1Y2hldCBNUycsICdHZW5ldmEgQ0UnLCBsdWNpZGEsIHNhbnMtc2VyaWY7DQoJYmFja2dyb3VuZCA6ICNmZmZmZmYgIWltcG9ydGFudCA7DQp9DQoudWktdGFicyB7DQoJcGFkZGluZzogLjJlbTsNCglwb3NpdGlvbjogcmVsYXRpdmU7DQoJem9vbTogMTsNCn0JCQkJCQkJCQ0KLmNsZWFyIHsgY2xlYXI6IGJvdGg7IGhlaWdodDogMDt9DQoubGF5b3V0TWFpbiB7DQoJbWFyZ2luOiAwcHggYXV0bzsNCglwYWRkaW5nOiA1cHggNXB4IDVweCA1cHg7CQ0KfQkJCQkNCi5sYXlvdXRSb3cgeyBtYXJnaW4tYm90dG9tOiA1cHg7IH0JCQkJDQouY2FwdGlvbiB7IC8qd2lkdGg6IDEwMCU7IGJvcmRlci1ib3R0b206IHNvbGlkIDFweCBibGFjazsqLyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24geyBib3JkZXI6IDBweCAhaW1wb3J0YW50OyB9DQoubm9jYXB0aW9uICZndDsgLmNhcHRpb24gc3BhbiB7DQoJYmFja2dyb3VuZDogbm9uZSAhaW1wb3J0YW50Ow0KCWRpc3BsYXk6IG5vbmU7DQp9IA0KLmNhcHRpb24gLnRpdGxlIHsgcGFkZGluZy1sZWZ0OiA1cHg7IH0NCi5oZWFkZXJjb3JyZWN0aW9uIHsJDQoJbWFyZ2luOiAwcHg7DQogICAgZm9udC1zaXplIDogMWVtOw0KICAgIGZvbnQtd2VpZ2h0OiBib2xkOw0KfQkJCQkNCi5sYWJlbFZpcyB7DQoJZmxvYXQ6IGxlZnQ7DQoJZm9udC13ZWlnaHQ6IGJvbGQ7DQoJZm9udC1mYW1pbHk6ICdPcGVuIFNhbnMnLCAnU2Vnb2UgVUknLCAnVHJlYnVjaGV0IE1TJywgJ0dlbmV2YSBDRScsIGx1Y2lkYSwgc2Fucy1zZXJpZjsNCglsaW5lLWhlaWdodDogMjVweDsNCgltYXJnaW46IDBweCAxOHB4IDBweCAwcHg7DQoJcGFkZGluZy1sZWZ0OiAzcHg7DQoJd2lkdGg6IDE5MHB4Ow0KCXdvcmQtd3JhcDogYnJlYWstd29yZDsNCiAgICBmb250LXNpemU6IDAuOGVtOw0KfQ0KLmNvbnRlbnRWaXMgeyAgICAJICAgICANCglmbG9hdDogbGVmdDsJDQoJbGluZS1oZWlnaHQ6IDI1cHg7DQoJbWFyZ2luOiAwcHg7DQoJcGFkZGluZzogMHB4Ow0KCXZlcnRpY2FsLWFsaWduOiB0b3A7DQogICAgZm9udC1zaXplOiAwLjc1ZW07CQkJDQp9DQoud29yZHdyYXAgeyANCiAgICB3aGl0ZS1zcGFjZTogcHJlLXdyYXA7ICAgICAgDQogICAgd2hpdGUtc3BhY2U6IC1tb3otcHJlLXdyYXA7IA0KICAgIHdoaXRlLXNwYWNlOiAtcHJlLXdyYXA7ICAgICANCiAgICB3aGl0ZS1zcGFjZTogLW8tcHJlLXdyYXA7ICAgDQogICAgd29yZC13cmFwOiBicmVhay13b3JkOyAgICAgIA0KfQkNCi51aS13aWRnZXQtY29udGVudCB7DQoJYmFja2dyb3VuZCA6IDUwJSA1MCUgcmVwZWF0LXggI2ZmZmZmZjsNCglib3JkZXIgOiAjZDRkNGQ0IHNvbGlkIDJweDsNCgljb2xvciA6ICM0ZjRlNGU7DQoJYm9yZGVyLXJhZGl1cyA6IDNweDsNCn0NCi51aS13aWRnZXQtaGVhZGVyIHsNCgljdXJzb3IgOiBwb2ludGVyOw0KCWZvbnQtc2l6ZSA6IDAuOGVtOw0KCWNvbG9yIDogIzQ5NDk0OTsNCglwYWRkaW5nLWxlZnQgOiAycHg7DQoJYm9yZGVyIDogI2VhZTllOCBzb2xpZCAxcHg7DQoJYmFja2dyb3VuZC1jb2xvciA6ICNlYWU5ZTg7DQoJbWFyZ2luLWJvdHRvbTogM3B4Ow0KCWJvcmRlci1yYWRpdXMgOiAzcHg7DQp9CQkJDQo8L3N0eWxlPg0KPC9oZWFkPg0KPGJvZHk+DQo8ZGl2IGlkPSJtYWluIiBjbGFzcz0ibGF5b3V0TWFpbiI+DQo8eHNsOmFwcGx5LXRlbXBsYXRlcy8+DQo8L2Rpdj4NCjwvYm9keT4NCjwvaHRtbD4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iL2Vnb25wOkdlbmVyYWxBZ2VuZGEiPg0KPGRpdiBjbGFzcz0ibGF5b3V0Um93IHVpLXRhYnMgdWktd2lkZ2V0LWNvbnRlbnQiID4NCjxkaXYgY2xhc3M9ImNhcHRpb24gdWktd2lkZ2V0LWhlYWRlciI+DQo8ZGl2IGNsYXNzPSJoZWFkZXJjb3JyZWN0aW9uIj5WxaFlb2JlY27DoSBhZ2VuZGE8L2Rpdj4NCjwvZGl2Pg0KPHhzbDphcHBseS10ZW1wbGF0ZXMgc2VsZWN0PSIuL2Vnb25wOnN1YmplY3QiLz4NCjx4c2w6YXBwbHktdGVtcGxhdGVzIHNlbGVjdD0iLi9lZ29ucDp0ZXh0Ii8+DQo8L2Rpdj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBtYXRjaD0iZWdvbnA6R2VuZXJhbEFnZW5kYS9lZ29ucDpzdWJqZWN0Ij4NCjx4c2w6aWYgdGVzdD0iLi90ZXh0KCkiPg0KPGRpdj48bGFiZWwgY2xhc3M9ImxhYmVsVmlzIj5QcmVkbWV0OiA8L2xhYmVsPjxzcGFuIGNsYXNzPSJjb250ZW50VmlzIHdvcmR3cmFwIj48eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj48eHNsOndpdGgtcGFyYW0gbmFtZT0idGV4dCIgc2VsZWN0PSIuIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiclMEEnIiAvPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJieSIgc2VsZWN0PSInJiMxMzsmIzEwOyciIC8+PC94c2w6Y2FsbC10ZW1wbGF0ZT48L3NwYW4+PC9kaXY+PGRpdiBjbGFzcz0iY2xlYXIiPiYjeGEwOzwvZGl2PjwveHNsOmlmPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG1hdGNoPSJlZ29ucDpHZW5lcmFsQWdlbmRhL2Vnb25wOnRleHQiPg0KPHhzbDppZiB0ZXN0PSIuL3RleHQoKSI+DQo8ZGl2PjxsYWJlbCBjbGFzcz0ibGFiZWxWaXMiPlRleHQ6IDwvbGFiZWw+PHNwYW4gY2xhc3M9ImNvbnRlbnRWaXMgd29yZHdyYXAiPjx4c2w6Y2FsbC10ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9Ii4iIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9InJlcGxhY2UiIHNlbGVjdD0iJyUwQSciIC8+PHhzbDp3aXRoLXBhcmFtIG5hbWU9ImJ5IiBzZWxlY3Q9IicmIzEzOyYjMTA7JyIgLz48L3hzbDpjYWxsLXRlbXBsYXRlPjwvc3Bhbj48L2Rpdj48ZGl2IGNsYXNzPSJjbGVhciI+JiN4YTA7PC9kaXY+PC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VG9Ta0RhdGUiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkZGF0ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVN0cmluZyAhPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVN0cmluZyk9MTAgYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCAxLCA0KSkpICE9ICdOYU4nICI+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iY29uY2F0KHN1YnN0cmluZygkZGF0ZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVTdHJpbmcsIDEsIDQpKSIgLz4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkZGF0ZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iYm9vbGVhbkNoZWNrYm94VG9TdHJpbmciPg0KPHhzbDpwYXJhbSBuYW1lPSJib29sVmFsdWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9ImJvb2xWYWx1ZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGJvb2xWYWx1ZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJ3RydWUnICI+DQo8eHNsOnRleHQ+w4FubzwveHNsOnRleHQ+DQo8L3hzbDp3aGVuPg0KPHhzbDp3aGVuIHRlc3Q9IiRib29sVmFsdWVTdHJpbmcgPSAnZmFsc2UnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOndoZW4gdGVzdD0iJGJvb2xWYWx1ZVN0cmluZyA9ICcxJyAiPg0KPHhzbDp0ZXh0PsOBbm88L3hzbDp0ZXh0Pg0KPC94c2w6d2hlbj4NCjx4c2w6d2hlbiB0ZXN0PSIkYm9vbFZhbHVlU3RyaW5nID0gJzAnICI+DQo8eHNsOnRleHQ+TmllPC94c2w6dGV4dD4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkYm9vbFZhbHVlU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUaW1lVHJpbVNlY29uZHMiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0aW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJ0aW1lU3RyaW5nIiBzZWxlY3Q9InN0cmluZygkdGltZSkiIC8+DQo8eHNsOmlmIHRlc3Q9IiR0aW1lU3RyaW5nICE9ICcnIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJzdWJzdHJpbmcoJHRpbWVTdHJpbmcsIDEsIDUpIiAvPg0KPC94c2w6aWY+DQo8L3hzbDp0ZW1wbGF0ZT4NCjx4c2w6dGVtcGxhdGUgbmFtZT0iZm9ybWF0VGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9InRpbWUiIC8+DQo8eHNsOnZhcmlhYmxlIG5hbWU9InRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCR0aW1lKSIgLz4NCjx4c2w6aWYgdGVzdD0iJHRpbWVTdHJpbmcgIT0gJyciPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZygkdGltZVN0cmluZywgMSwgOCkiIC8+DQo8L3hzbDppZj4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJzdHJpbmctcmVwbGFjZS1hbGwiPg0KPHhzbDpwYXJhbSBuYW1lPSJ0ZXh0Ii8+DQo8eHNsOnBhcmFtIG5hbWU9InJlcGxhY2UiLz4NCjx4c2w6cGFyYW0gbmFtZT0iYnkiLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9ImNvbnRhaW5zKCR0ZXh0LCAkcmVwbGFjZSkiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9InN1YnN0cmluZy1iZWZvcmUoJHRleHQsJHJlcGxhY2UpIi8+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGJ5Ii8+DQo8eHNsOmNhbGwtdGVtcGxhdGUgbmFtZT0ic3RyaW5nLXJlcGxhY2UtYWxsIj4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJ0ZXh0IiBzZWxlY3Q9InN1YnN0cmluZy1hZnRlcigkdGV4dCwkcmVwbGFjZSkiLz4NCjx4c2w6d2l0aC1wYXJhbSBuYW1lPSJyZXBsYWNlIiBzZWxlY3Q9IiRyZXBsYWNlIi8+DQo8eHNsOndpdGgtcGFyYW0gbmFtZT0iYnkiIHNlbGVjdD0iJGJ5IiAvPg0KPC94c2w6Y2FsbC10ZW1wbGF0ZT4NCjwveHNsOndoZW4+DQo8eHNsOm90aGVyd2lzZT4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSIkdGV4dCIvPg0KPC94c2w6b3RoZXJ3aXNlPg0KPC94c2w6Y2hvb3NlPg0KPC94c2w6dGVtcGxhdGU+DQo8eHNsOnRlbXBsYXRlIG5hbWU9ImZvcm1hdFRvU2tEYXRlVGltZSI+DQo8eHNsOnBhcmFtIG5hbWU9ImRhdGVUaW1lIiAvPg0KPHhzbDp2YXJpYWJsZSBuYW1lPSJkYXRlVGltZVN0cmluZyIgc2VsZWN0PSJzdHJpbmcoJGRhdGVUaW1lKSIgLz4NCjx4c2w6Y2hvb3NlPg0KPHhzbDp3aGVuIHRlc3Q9IiRkYXRlVGltZVN0cmluZyE9ICcnIGFuZCBzdHJpbmctbGVuZ3RoKCRkYXRlVGltZVN0cmluZyk+MTggYW5kIHN0cmluZyhudW1iZXIoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCkpKSAhPSAnTmFOJyAiPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9ImNvbmNhdChzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA5LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCA2LCAyKSwgJy4nLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxLCA0KSwnICcsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEyLCAyKSwnOicsIHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDE1LCAyKSkiIC8+DQo8L3hzbDp3aGVuPg0KPHhzbDpvdGhlcndpc2U+DQo8eHNsOnZhbHVlLW9mIHNlbGVjdD0iJGRhdGVUaW1lU3RyaW5nIj48L3hzbDp2YWx1ZS1vZj4NCjwveHNsOm90aGVyd2lzZT4NCjwveHNsOmNob29zZT4NCjwveHNsOnRlbXBsYXRlPg0KPHhzbDp0ZW1wbGF0ZSBuYW1lPSJmb3JtYXRUb1NrRGF0ZVRpbWVTZWNvbmQiPg0KPHhzbDpwYXJhbSBuYW1lPSJkYXRlVGltZSIgLz4NCjx4c2w6dmFyaWFibGUgbmFtZT0iZGF0ZVRpbWVTdHJpbmciIHNlbGVjdD0ic3RyaW5nKCRkYXRlVGltZSkiIC8+DQo8eHNsOmNob29zZT4NCjx4c2w6d2hlbiB0ZXN0PSIkZGF0ZVRpbWVTdHJpbmchPSAnJyBhbmQgc3RyaW5nLWxlbmd0aCgkZGF0ZVRpbWVTdHJpbmcpPjE4IGFuZCBzdHJpbmcobnVtYmVyKHN1YnN0cmluZygkZGF0ZVRpbWVTdHJpbmcsIDEsIDQpKSkgIT0gJ05hTicgIj4NCjx4c2w6dmFsdWUtb2Ygc2VsZWN0PSJjb25jYXQoc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgOSwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgNiwgMiksICcuJywgc3Vic3RyaW5nKCRkYXRlVGltZVN0cmluZywgMSwgNCksJyAnLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxMiwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxNSwgMiksJzonLCBzdWJzdHJpbmcoJGRhdGVUaW1lU3RyaW5nLCAxOCwgMikpIiAvPg0KPC94c2w6d2hlbj4NCjx4c2w6b3RoZXJ3aXNlPg0KPHhzbDp2YWx1ZS1vZiBzZWxlY3Q9IiRkYXRlVGltZVN0cmluZyI+PC94c2w6dmFsdWUtb2Y+DQo8L3hzbDpvdGhlcndpc2U+DQo8L3hzbDpjaG9vc2U+DQo8L3hzbDp0ZW1wbGF0ZT4NCjwveHNsOnN0eWxlc2hlZXQ+DQoNCg==";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
                null,
                null,
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null,
                DigestAlgorithm.SHA256,
                null,
                null,
                null,
                null,
                xsdSchema,
                transformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64WithoutXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64WithoutXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64WithValidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC9HZW5lcmFsQWdlbmRhPjwveGRjOlhNTERhdGE+PHhkYzpVc2VkU2NoZW1hc1JlZmVyZW5jZWQ+PHhkYzpVc2VkWFNEUmVmZXJlbmNlIERpZ2VzdE1ldGhvZD0idXJuOm9pZDoyLjE2Ljg0MC4xLjEwMS4zLjQuMi4xIiBEaWdlc3RWYWx1ZT0iL0N0bjBCOUQ3SEtuNlVSRlI4aVBVS2Z5R2U0bUJZcEsrMjVkYzFpWVd1RT0iIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzZDwveGRjOlVzZWRYU0RSZWZlcmVuY2U+PHhkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlIENvbnRlbnRUeXBlPSJhcHBsaWNhdGlvbi94c2x0K3htbCIgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSJRbzFqWVgxSld5ZHZNL09ML3JuaXJwaGsxck0xejQxZlBSWEJFZ3AvcWJnPSIgTGFuZ3VhZ2U9InNrIiBNZWRpYURlc3RpbmF0aW9uVHlwZURlc2NyaXB0aW9uPSJUWFQiIFRyYW5zZm9ybUFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiPmh0dHA6Ly9zY2hlbWFzLmdvdi5zay9mb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOS9mb3JtLnhzbHQ8L3hkYzpVc2VkUHJlc2VudGF0aW9uU2NoZW1hUmVmZXJlbmNlPjwveGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48L3hkYzpYTUxEYXRhQ29udGFpbmVyPg==";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCInAsiceBase64WithValidXmlAgainstXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXDCBase64WithInvalidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eGRjOlhNTERhdGFDb250YWluZXIgeG1sbnM6eGRjPSJodHRwOi8vZGF0YS5nb3Yuc2svZGVmL2NvbnRhaW5lci94bWxkYXRhY29udGFpbmVyK3htbC8xLjEiPjx4ZGM6WE1MRGF0YSBDb250ZW50VHlwZT0iYXBwbGljYXRpb24veG1sOyBjaGFyc2V0PVVURi04IiBJZGVudGlmaWVyPSJodHRwOi8vZGF0YS5nb3Yuc2svZG9jL2Vmb3JtL0FwcC5HZW5lcmFsQWdlbmRhLzEuOSIgVmVyc2lvbj0iMS45Ij48c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pjx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+PC94ZGM6WE1MRGF0YT48eGRjOlVzZWRTY2hlbWFzUmVmZXJlbmNlZD48eGRjOlVzZWRYU0RSZWZlcmVuY2UgRGlnZXN0TWV0aG9kPSJ1cm46b2lkOjIuMTYuODQwLjEuMTAxLjMuNC4yLjEiIERpZ2VzdFZhbHVlPSIvQ3RuMEI5RDdIS242VVJGUjhpUFVLZnlHZTRtQllwSysyNWRjMWlZV3VFPSIgVHJhbnNmb3JtQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy9UUi8yMDAxL1JFQy14bWwtYzE0bi0yMDAxMDMxNSI+aHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45L2Zvcm0ueHNkPC94ZGM6VXNlZFhTRFJlZmVyZW5jZT48eGRjOlVzZWRQcmVzZW50YXRpb25TY2hlbWFSZWZlcmVuY2UgQ29udGVudFR5cGU9ImFwcGxpY2F0aW9uL3hzbHQreG1sIiBEaWdlc3RNZXRob2Q9InVybjpvaWQ6Mi4xNi44NDAuMS4xMDEuMy40LjIuMSIgRGlnZXN0VmFsdWU9IlFvMWpZWDFKV3lkdk0vT0wvcm5pcnBoazFyTTF6NDFmUFJYQkVncC9xYmc9IiBMYW5ndWFnZT0ic2siIE1lZGlhRGVzdGluYXRpb25UeXBlRGVzY3JpcHRpb249IlRYVCIgVHJhbnNmb3JtQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy9UUi8yMDAxL1JFQy14bWwtYzE0bi0yMDAxMDMxNSI+aHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45L2Zvcm0ueHNsdDwveGRjOlVzZWRQcmVzZW50YXRpb25TY2hlbWFSZWZlcmVuY2U+PC94ZGM6VXNlZFNjaGVtYXNSZWZlcmVuY2VkPjwveGRjOlhNTERhdGFDb250YWluZXI+";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48eHM6c2NoZW1hIGVsZW1lbnRGb3JtRGVmYXVsdD0icXVhbGlmaWVkIiBhdHRyaWJ1dGVGb3JtRGVmYXVsdD0idW5xdWFsaWZpZWQiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgdGFyZ2V0TmFtZXNwYWNlPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiIHhtbG5zPSJodHRwOi8vc2NoZW1hcy5nb3Yuc2svZm9ybS9BcHAuR2VuZXJhbEFnZW5kYS8xLjkiPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJ0ZXh0QXJlYSI+Cjx4czpyZXN0cmljdGlvbiBiYXNlPSJ4czpzdHJpbmciPgo8L3hzOnJlc3RyaWN0aW9uPgo8L3hzOnNpbXBsZVR5cGU+Cjx4czpzaW1wbGVUeXBlIG5hbWU9Im1lbm8iPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgoKCjx4czplbGVtZW50IG5hbWU9IkdlbmVyYWxBZ2VuZGEiPgo8eHM6Y29tcGxleFR5cGU+Cjx4czpzZXF1ZW5jZT4KPHhzOmVsZW1lbnQgbmFtZT0ic3ViamVjdCIgdHlwZT0ibWVubyIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPHhzOmVsZW1lbnQgbmFtZT0idGV4dCIgdHlwZT0idGV4dEFyZWEiIG1pbk9jY3Vycz0iMCIgbmlsbGFibGU9InRydWUiIC8+CjwveHM6c2VxdWVuY2U+CjwveHM6Y29tcGxleFR5cGU+CjwveHM6ZWxlbWVudD4KPC94czpzY2hlbWE+";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                xsltTransformation,
                defaultIdentifier,
                false,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.gov.sk.xmldatacontainer+xml; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertThrows(MalformedBodyException.class, () -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlWithoutXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n<subject>Nové podanie</subject>\n<text>Podávam toto nové podanie.</text>\n</root>\n";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlInvalidXmlAgainstXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n  <subject>Nové podanie</subject>\n  <text>Podávam toto nové podanie.</text>\n</root>\n";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>\n";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlValidXmlAgainstXSD() {
        var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><GeneralAgenda xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n  <subject>Nové podanie</subject>\n  <text>Podávam toto nové podanie.</text>\n</GeneralAgenda>";
        var document = new Document(xmlContent);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>\n";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/xml";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlBase64WithoutXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KICA8c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0PgogIDx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+CjwvR2VuZXJhbEFnZW5kYT4=";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/xml;base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlInAsiceBase64WithoutXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlBase64InvalidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxyb290Pg0KICA8c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0Pg0KICA8dGV4dD5Qb2TDoXZhbSB0b3RvIG5vdsOpIHBvZGFuaWUuPC90ZXh0Pg0KPC9yb290Pg0K";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHhzOnNjaGVtYSBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHRhcmdldE5hbWVzcGFjZT0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KPHhzOnNpbXBsZVR5cGUgbmFtZT0idGV4dEFyZWEiPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJtZW5vIj4KPHhzOnJlc3RyaWN0aW9uIGJhc2U9InhzOnN0cmluZyI+CjwveHM6cmVzdHJpY3Rpb24+CjwveHM6c2ltcGxlVHlwZT4KPHhzOmVsZW1lbnQgbmFtZT0iR2VuZXJhbEFnZW5kYSI+Cjx4czpjb21wbGV4VHlwZT4KPHhzOnNlcXVlbmNlPgo8eHM6ZWxlbWVudCBuYW1lPSJzdWJqZWN0IiB0eXBlPSJtZW5vIiBtaW5PY2N1cnM9IjAiIG5pbGxhYmxlPSJ0cnVlIiAvPgo8eHM6ZWxlbWVudCBuYW1lPSJ0ZXh0IiB0eXBlPSJ0ZXh0QXJlYSIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPC94czpzZXF1ZW5jZT4KPC94czpjb21wbGV4VHlwZT4KPC94czplbGVtZW50Pgo8L3hzOnNjaGVtYT4K";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/xml;base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlBase64ValidXmlAgainstXSD() {
        var xmlContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48R2VuZXJhbEFnZW5kYSB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KICA8c3ViamVjdD5Ob3bDqSBwb2RhbmllPC9zdWJqZWN0PgogIDx0ZXh0PlBvZMOhdmFtIHRvdG8gbm92w6kgcG9kYW5pZS48L3RleHQ+CjwvR2VuZXJhbEFnZW5kYT4=";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHhzOnNjaGVtYSBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHRhcmdldE5hbWVzcGFjZT0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KPHhzOnNpbXBsZVR5cGUgbmFtZT0idGV4dEFyZWEiPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJtZW5vIj4KPHhzOnJlc3RyaWN0aW9uIGJhc2U9InhzOnN0cmluZyI+CjwveHM6cmVzdHJpY3Rpb24+CjwveHM6c2ltcGxlVHlwZT4KPHhzOmVsZW1lbnQgbmFtZT0iR2VuZXJhbEFnZW5kYSI+Cjx4czpjb21wbGV4VHlwZT4KPHhzOnNlcXVlbmNlPgo8eHM6ZWxlbWVudCBuYW1lPSJzdWJqZWN0IiB0eXBlPSJtZW5vIiBtaW5PY2N1cnM9IjAiIG5pbGxhYmxlPSJ0cnVlIiAvPgo8eHM6ZWxlbWVudCBuYW1lPSJ0ZXh0IiB0eXBlPSJ0ZXh0QXJlYSIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPC94czpzZXF1ZW5jZT4KPC94czpjb21wbGV4VHlwZT4KPC94czplbGVtZW50Pgo8L3hzOnNjaGVtYT4K";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/xml;base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateXmlInAsiceBase64ValidXmlAgainstXSD() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var schema = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHhzOnNjaGVtYSBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHRhcmdldE5hbWVzcGFjZT0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45IiB4bWxucz0iaHR0cDovL3NjaGVtYXMuZ292LnNrL2Zvcm0vQXBwLkdlbmVyYWxBZ2VuZGEvMS45Ij4KPHhzOnNpbXBsZVR5cGUgbmFtZT0idGV4dEFyZWEiPgo8eHM6cmVzdHJpY3Rpb24gYmFzZT0ieHM6c3RyaW5nIj4KPC94czpyZXN0cmljdGlvbj4KPC94czpzaW1wbGVUeXBlPgo8eHM6c2ltcGxlVHlwZSBuYW1lPSJtZW5vIj4KPHhzOnJlc3RyaWN0aW9uIGJhc2U9InhzOnN0cmluZyI+CjwveHM6cmVzdHJpY3Rpb24+CjwveHM6c2ltcGxlVHlwZT4KPHhzOmVsZW1lbnQgbmFtZT0iR2VuZXJhbEFnZW5kYSI+Cjx4czpjb21wbGV4VHlwZT4KPHhzOnNlcXVlbmNlPgo8eHM6ZWxlbWVudCBuYW1lPSJzdWJqZWN0IiB0eXBlPSJtZW5vIiBtaW5PY2N1cnM9IjAiIG5pbGxhYmxlPSJ0cnVlIiAvPgo8eHM6ZWxlbWVudCBuYW1lPSJ0ZXh0IiB0eXBlPSJ0ZXh0QXJlYSIgbWluT2NjdXJzPSIwIiBuaWxsYWJsZT0idHJ1ZSIgLz4KPC94czpzZXF1ZW5jZT4KPC94czpjb21wbGV4VHlwZT4KPC94czplbGVtZW50Pgo8L3hzOnNjaGVtYT4K";
        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }

    @Test
    void testValidateTxtInAsiceBase64() {
        var xmlContent = "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";
        var document = new Document(xmlContent);

        var signingParameters = new ServerSigningParameters(
                ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B,
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        var payloadMimeType = "application/vnd.etsi.asic-e+zip; base64";

        SignRequestBody signRequestBody = new SignRequestBody(document, signingParameters, payloadMimeType);

        Assertions.assertDoesNotThrow(() -> {signRequestBody.getParameters(null, true);});
    }
}
