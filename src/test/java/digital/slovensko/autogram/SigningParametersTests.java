package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.*;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigningParametersTests {
    private String inclusive;
    private ASiCContainerType asice;
    private SignaturePackaging enveloping;
    private String xdcXmlns;

    private byte[] generalAgendaXml;
    private String xsdSchema;
    private String xsltTransformation;
    private String identifier;
    private TSPSource tspSource;
    private EFormAttributes attributes;

    @BeforeAll
    void setDefaultValues() throws IOException {
        inclusive = CanonicalizationMethod.INCLUSIVE;
        asice = ASiCContainerType.ASiC_E;
        enveloping = SignaturePackaging.ENVELOPING;
        xdcXmlns = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";

        generalAgendaXml = this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        xsdSchema = new String(this.getClass().getResourceAsStream("general_agenda.xsd").readAllBytes(), StandardCharsets.UTF_8);
        xsltTransformation = new String(
                this.getClass().getResourceAsStream("general_agenda.xslt").readAllBytes(), StandardCharsets.UTF_8);
        identifier = "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9";
        tspSource = null;

        attributes = new EFormAttributes("id1/asa", null, null, xdcXmlns, null, null, false);
    }

    @Test
    void testThrowsAutogramExceptionWhenNoMimeType() throws IOException {
        var document = new InMemoryDocument(generalAgendaXml);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, attributes, false, null, false, 800, document, tspSource, true));
    }

    @Test
    void testThrowsAutogramExceptionWhenNoDocument() {
        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, attributes, false, null, false, 800, null, tspSource, true));
    }

    @Test
    void testThrowsXMLValidationFailedWhenNoXML() {
        var document = new InMemoryDocument("not xml".getBytes(), "doc.xml", MimeTypeEnum.XML);

        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document, tspSource, true));
    }

    @Test
    void testThrowsAutogramExceptionWhenNoSignatureLevel() {
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildParameters(null, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, attributes, false, null, false, 800, null, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesNoConatiner(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null,
                        false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesInAsice(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, null, null, null, null,
                        false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesXdcInAsiceWith(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesXdcInAsiceAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXml(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlWithAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#nonEFormXmlProvider")
    void testThrowsUnknownEformExceptionWithInvalidXmlEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document, tspSource, false));
    }


    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlSchema(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlSchemaWithAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testThrowsAutogramExceptionWithUnknownEformXml(DSSDocument document) {
        Assertions.assertThrows(EFormException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, null, null, null, new EFormAttributes(null, null, null,
                        xdcXmlns, null, null, false), false,
                        null, false, 800, document, tspSource, false));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#unknownEfomXmlProvider")
    void testThrowsAutogramExceptionWithUnknownEformXmlWithAutoLoadEform(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document, tspSource, false));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#mismatchedDigestsXmlProvider")
    void testThrowsAutogramExceptionWithMismatchedDigestsXml(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#mismatchedDigestsXmlProvider",
            "digital.slovensko.autogram.TestMethodSources#mismatchedDigestsFSXmlProvider"})
    void testThrowsAutogramExceptionWithMismatchedDigestsXmlWithAutoLoadEform(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        "792_772", false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidAsiceProvider")
    void testThrowsOriginalDocumentNotFoundWithAsiceWithoutSignature(DSSDocument document) throws IOException {
        Assertions.assertThrows(OriginalDocumentNotFoundException.class,
                () -> SigningParameters.buildForASiCWithXAdES(document, false, false, tspSource, true));
    }

    @Test
    void testThrowsExceptionWithAsiceWithEmptyXml() throws IOException {
        var document = new InMemoryDocument(
                this.getClass().getResourceAsStream("empty_xml.asice").readAllBytes(),
                "empty_xml.asice");

        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildForASiCWithXAdES(document, false, false, tspSource, false));
    }

    @Test
    void testInvalidTransformation() throws IOException {
        var generalAgendaXml = this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);

        var transformation = "invalid transformation";
        Assertions.assertThrows(TransformationParsingErrorException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, transformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document, tspSource, true));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#embeddedOrsrDocumentsProvider")
    void testDoesNotThrowWithEmbeddedXdcWithoutAutoLoad(DSSDocument document) {
        var eFormAttributes = new EFormAttributes(identifier, "", "", "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", null, null, true);

        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, eFormAttributes, false, null, false, 800, document, tspSource, false));
    }

    @Test
    public void testXDCValidationWithValidTransformationHash() throws Exception {
        var xdcContent = getClass().getResourceAsStream("general_agenda_xdc_indented.xml").readAllBytes();
        var xdcDocument = new InMemoryDocument(xdcContent, "test.xml", AutogramMimeType.XML_DATACONTAINER);

        var params = SigningParameters.buildParameters(
                SignatureLevel.XAdES_BASELINE_B,
                DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING,
                false,
                null,
                null,
                null,
                null,
                true,
                null,
                false,
                640,
                xdcDocument,
                null,
                false
        );

        Assertions.assertNotNull(params);
        Assertions.assertEquals(SignatureLevel.XAdES_BASELINE_B, params.getLevel());
    }

    @Test
    public void testXDCValidationWithMismatchedXsltHash() throws Exception {
        var xdcContent = getClass().getResourceAsStream("fs_forms/d_fs792_772_xdc_xslt_digest.xml").readAllBytes();
        var xdcDocument = new InMemoryDocument(xdcContent, "test.xml", AutogramMimeType.XML_DATACONTAINER);

        Assertions.assertThrows(XMLValidationException.class, () ->
            SigningParameters.buildParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    null,
                    null,
                    null,
                    null,
                    true,
                    null,
                    false,
                    640,
                    xdcDocument,
                    null,
                    false
            )
        );
    }

    @Test
    public void testXDCValidationWithMismatchedXsdHash() throws Exception {
        var xdcContent = getClass().getResourceAsStream("fs_forms/d_fs792_772_xdc_xsd_digest.xml").readAllBytes();
        var xdcDocument = new InMemoryDocument(xdcContent, "test.xml", AutogramMimeType.XML_DATACONTAINER);

        Assertions.assertThrows(XMLValidationException.class, () ->
            SigningParameters.buildParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    null,
                    null,
                    null,
                    null,
                    true,
                    null,
                    false,
                    640,
                    xdcDocument,
                    null,
                    false
            )
        );
    }

    @Test
    public void testXDCValidationWithWrongSchema() throws Exception {
        var xdcContent = getClass().getResourceAsStream("wrong_schema_ga_xdc.xml").readAllBytes();
        var xdcDocument = new InMemoryDocument(xdcContent, "test.xml", AutogramMimeType.XML_DATACONTAINER);

        Assertions.assertThrows(XMLValidationException.class, () ->
            SigningParameters.buildParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    null,
                    null,
                    null,
                    null,
                    true,
                    null,
                    false,
                    640,
                    xdcDocument,
                    null,
                    false
            )
        );
    }

    @Test
    public void testXDCValidationWithEmbedUsedSchemas() throws Exception {
        var xdcContent = getClass().getResourceAsStream("fs_forms/d_fs792_772_xdc_xslt_digest.xml").readAllBytes();
        var xdcDocument = new InMemoryDocument(xdcContent, "test.xml", AutogramMimeType.XML_DATACONTAINER);

        Assertions.assertThrows(XMLValidationException.class, () ->
            SigningParameters.buildParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    null,
                    null,
                    null,
                    null,
                    true,
                    null,
                    false,
                    640,
                    xdcDocument,
                    null,
                    false
            )
        );
    }

    @Test
    public void testXDCValidationIsCalledForXDCContent() throws Exception {
        var xdcContent = getClass().getResourceAsStream("general_agenda_xdc_indented.xml").readAllBytes();
        var xdcDocument = new InMemoryDocument(xdcContent, "test.xml", AutogramMimeType.APPLICATION_XML);

        var params = SigningParameters.buildParameters(
                SignatureLevel.XAdES_BASELINE_B,
                DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING,
                false,
                null,
                null,
                null,
                null,
                true,
                null,
                false,
                640,
                xdcDocument,
                null,
                false
        );

        Assertions.assertNotNull(params);
        Assertions.assertTrue(params.shouldCreateXdc());
    }

    @Test
    public void testPlainXMLWithoutTransformationThrowsException() throws Exception {
        var xmlContent = getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        var xmlDocument = new InMemoryDocument(xmlContent, "test.xml", AutogramMimeType.APPLICATION_XML);

        Assertions.assertThrows(Exception.class, () ->
            SigningParameters.buildParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    false,
                    640,
                    xmlDocument,
                    null,
                    false  // plainXmlEnabled = false
            )
        );
    }

    @Test
    public void testPlainXMLWithTransformationIsAllowed() throws Exception {
        var xmlContent = getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        var xmlDocument = new InMemoryDocument(xmlContent, "test.xml", AutogramMimeType.APPLICATION_XML);

        var params = SigningParameters.buildParameters(
                SignatureLevel.XAdES_BASELINE_B,
                DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING,
                false,
                null,
                null,
                null,
                null,
                true,
                null,
                false,
                640,
                xmlDocument,
                null,
                true  // plainXmlEnabled = true
        );

        Assertions.assertNotNull(params);
    }
}

