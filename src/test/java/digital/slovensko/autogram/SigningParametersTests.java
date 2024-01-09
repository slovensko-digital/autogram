package digital.slovensko.autogram;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;

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
    }

    @Test
    void testThrowsAutogramExceptionWhenNoMimeType() throws IOException {
        var document = new InMemoryDocument(generalAgendaXml);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, enveloping,
                        null, false, inclusive, inclusive, inclusive, null, null, "id1/asa", false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @Test
    void testThrowsAutogramExceptionWhenNoDocument() {
        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, enveloping,
                        null, false, inclusive, inclusive, inclusive, null, "<xml/>", "id1/asa", false, 800, false, false,
                        null, null, null, null, null, null, tspSource));
    }

    @Test
    void testThrowsXMLValidationFailedWhenNoXML() {
        var document = new InMemoryDocument("not xml".getBytes(), "doc.xml", MimeTypeEnum.XML);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, enveloping,
                        null, false, inclusive, inclusive, inclusive, null, xsltTransformation, "id1/asa", false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @Test
    void testThrowsAutogramExceptionWhenNoSignatureLevel() {
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildFromRequest(null, asice, xdcXmlns, enveloping, null, false, inclusive,
                        inclusive, inclusive, null, "<xml/>", "id1/asa", false, 800, false, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesNoConatiner(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        null, false, null, null, null, null, null, null, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesInAsice(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, null, null,
                        null, false, null, null, null, null, null, null, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesXdcInAsiceWith(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, null, null,
                        false, null, null, null, xsdSchema, xsltTransformation, identifier, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesXdcInAsiceAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, null, null, null, null,
                        false, null, null, null, null, null, null, false, 800, true, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXml(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, null, null,
                        false, null, null, null, xsdSchema, xsltTransformation, identifier, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlWithAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, null, null, null, null,
                        false, null, null, null, null, null, null, false, 800, true, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlSchema(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, null, null,
                        false, null, null, null, xsdSchema, xsltTransformation, identifier, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlSchemaWithAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, null, null, null, null,
                        false, null, null, null, null, null, null, false, 800, true, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void testThrowsAutogramExceptionWithUnknownEformXml(DSSDocument document) {
        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, null, null,
                        false, null, null, null, null, null, null, false, 800, false, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#unknownEfomXmlProvider")
    void testThrowsAutogramExceptionWithUnknownEformXmlWithAutoLoadEform(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, null, null, null, null,
                        false, null, null, null, null, null, null, false, 800, true, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#mismatchedDigestsXmlProvider")
    void testThrowsAutogramExceptionWithMismatchedDigestsXml(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, null, null,
                        false, null, null, null, xsdSchema, xsltTransformation, identifier, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#mismatchedDigestsXmlProvider")
    void testThrowsAutogramExceptionWithMismatchedDigestsXmlWithAutoLoadEform(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, null, null, null, null,
                        false, null, null, null, null, null, null, false, 800, true, false, null, null, null, null, null, document, tspSource));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidAsiceProvider")
    void testThrowsOriginalDocumentNotFoundWithAsiceWithoutSignature(DSSDocument document) throws IOException {
        Assertions.assertThrows(OriginalDocumentNotFoundException.class,
                () -> SigningParameters.buildForASiCWithXAdES(document, false, tspSource));
    }

    @Test
    void testThrowsExceptionWithAsiceWithEmptyXml() throws IOException {
        var document = new InMemoryDocument(
                this.getClass().getResourceAsStream("empty_xml.asice").readAllBytes(),
                "empty_xml.asice");

        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildForASiCWithXAdES(document, false, tspSource));
    }

    @Test
    void testInvalidTransformation() throws IOException {
        var generalAgendaXml = this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);

        var transformation = "invalid transformation";
        Assertions.assertThrows(TransformationParsingErrorException.class,
                () -> SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B, asice, xdcXmlns, null, null,
                        false, null, null, null, xsdSchema, transformation, identifier, false, 800, false, false,
                        null, null, null, null, null, document, tspSource));
    }

}
