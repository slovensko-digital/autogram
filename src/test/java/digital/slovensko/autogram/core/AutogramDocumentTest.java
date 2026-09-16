package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.AutogramMimeType;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.EFormException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

class AutogramDocumentTest {
    private static final String IDENTIFIER = "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9";
    private static final String XDC_XMLNS = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";

    private static byte[] generalAgendaXml;
    private static String xsdSchema;
    private static String xsltTransformation;

    @BeforeAll
    static void loadFixtures() throws IOException {
        generalAgendaXml = TestMethodSources.loadContent("general_agenda.xml");
        xsdSchema = new String(TestMethodSources.loadContent("general_agenda.xsd"), StandardCharsets.UTF_8);
        xsltTransformation = new String(TestMethodSources.loadContent("general_agenda.xslt"), StandardCharsets.UTF_8);
    }

    @Test
    void throwsWhenDocumentHasNoMimeType() {
        var document = new InMemoryDocument(generalAgendaXml);

        assertThrows(SigningParametersException.class, () -> AutogramDocument.build(document, attributes(null)));
    }

    @Test
    void throwsWhenDocumentIsNull() {
        assertThrows(NullPointerException.class, () -> AutogramDocument.build(null, attributes(XDC_XMLNS)));
    }

    @Test
    void throwsWhenXmlIsMalformed() {
        var document = new InMemoryDocument("not xml".getBytes(), "doc.xml", MimeTypeEnum.XML);

        assertThrows(XMLValidationException.class, () -> AutogramDocument.build(document, attributes(XDC_XMLNS)));
    }

    @Test
    void throwsWhenTransformationIsInvalid() {
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);
        var attributes = new EFormAttributes(IDENTIFIER, "invalid transformation", xsdSchema, XDC_XMLNS, null, null,
                false, null, false, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256);

        assertThrows(TransformationParsingErrorException.class, () -> AutogramDocument.build(document, attributes));
    }

    @Test
    void rejectsSigningTransformationWithoutXdcNamespace() throws IOException {
        var transformation = new String(TestMethodSources.loadContent("crystal_test_data/PovolenieZdravotnictvo.sb.xslt"));
        var schema = new String(TestMethodSources.loadContent("crystal_test_data/rozhodnutie_X4564-2.xsd"));
        var document = new InMemoryDocument(TestMethodSources.loadContent("crystal_test_data/rozhodnutie_X4564-2.xml"), "rozhodnutie_X4564-2.xml");
        var attributes = new EFormAttributes(
                "id1/asa", transformation, schema, null, null, null, false, null, false,
                CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256);

        assertThrows(SigningParametersException.class, () -> AutogramDocument.build(document, attributes));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidXmlProvider")
    void rejectsInvalidXmlWithExplicitEformResources(DSSDocument document) {
        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, explicitAttributes(document)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidXmlProvider")
    void rejectsInvalidXmlWithAutomaticallyLoadedEformResources(DSSDocument document) {
        var parameters = parameters(true);

        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#nonEFormXmlProvider")
    void rejectsMalformedNonEformXml(DSSDocument document) {
        var parameters = parameters(false);

        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void rejectsXmlThatDoesNotMatchExplicitSchema(DSSDocument document) {
        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, explicitAttributes(document)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void rejectsXmlThatDoesNotMatchAutomaticallyLoadedSchema(DSSDocument document) {
        var parameters = parameters(true);

        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void rejectsIncompleteEformUnlessAsiceResourcesCanBeLoaded(DSSDocument document) {
        var parameters = parameters(false);
        var incompleteAttributes = new EFormAttributes(null, null, null, XDC_XMLNS, null, null, false, null,
                AutogramMimeType.isAsice(document.getMimeType()), parameters.getPropertiesCanonicalization(),
                parameters.getDigestAlgorithm());
        Executable buildDocument = () -> AutogramDocument.build(document, incompleteAttributes);

        if (AutogramMimeType.isAsice(document.getMimeType()))
            assertDoesNotThrow(buildDocument);
        else
            assertThrows(EFormException.class, buildDocument);
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#unknownEfomXmlProvider")
    void rejectsUnknownEformDuringAutomaticLoading(DSSDocument document) {
        var parameters = parameters(false);

        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#mismatchedDigestsXmlProvider")
    void rejectsMismatchedDigestsWithExplicitEformResources(DSSDocument document) {
        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, explicitAttributes(document)));
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#mismatchedDigestsXmlProvider",
            "digital.slovensko.autogram.TestMethodSources#mismatchedDigestsFSXmlProvider" })
    void rejectsMismatchedDigestsWithAutomaticallyLoadedEformResources(DSSDocument document) {
        var parameters = parameters(true);
        var attributes = new EFormAttributes(null, null, null, null, null, null, false, "792_772", true,
                parameters.getPropertiesCanonicalization(), parameters.getDigestAlgorithm());

        assertThrows(XMLValidationException.class, () -> AutogramDocument.build(document, attributes));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#invalidAsiceProvider")
    void rejectsAsiceWithoutOriginalDocument(DSSDocument document) {
        var parameters = parameters(true);

        assertThrows(OriginalDocumentNotFoundException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    @Test
    void rejectsAsiceWithEmptyXml() throws IOException {
        var document = new InMemoryDocument(TestMethodSources.loadContent("empty_xml.asice"), "empty_xml.asice");
        var parameters = parameters(false);

        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#embeddedOrsrDocumentsProvider")
    void acceptsEmbeddedXdcWithoutAutomaticLoading(DSSDocument document) {
        var parameters = parameters(false);
        var attributes = new EFormAttributes(IDENTIFIER, "", "", XDC_XMLNS, null, null, true, null,
                AutogramMimeType.isAsice(document.getMimeType()), parameters.getPropertiesCanonicalization(),
                parameters.getDigestAlgorithm());

        assertDoesNotThrow(() -> AutogramDocument.build(document, attributes));
    }

    @Test
    void acceptsXdcWithValidTransformationHash() throws IOException {
        var document = new InMemoryDocument(TestMethodSources.loadContent("general_agenda_xdc_indented.xml"),
                "test.xml", AutogramMimeType.XML_DATACONTAINER);
        var parameters = parameters(false);

        var result = AutogramDocument.build(document, EFormAttributes.build(parameters, true));

        assertNotNull(result);
    }

    @Test
    void rejectsXdcWithMismatchedXsltHash() throws IOException {
        assertXdcValidationFails("fs_forms/d_fs792_772_xdc_xslt_digest.xml");
    }

    @Test
    void rejectsXdcWithMismatchedXsdHash() throws IOException {
        assertXdcValidationFails("fs_forms/d_fs792_772_xdc_xsd_digest.xml");
    }

    @Test
    void rejectsXdcWithWrongSchema() throws IOException {
        assertXdcValidationFails("wrong_schema_ga_xdc.xml");
    }

    @Test
    void validatesEmbeddedSchemasInXdc() throws IOException {
        assertXdcValidationFails("fs_forms/d_fs792_772_xdc_xslt_digest.xml");
    }

    @Test
    void detectsXdcContentWithApplicationXmlMimeType() throws IOException {
        var document = new InMemoryDocument(TestMethodSources.loadContent("general_agenda_xdc_indented.xml"),
                "test.xml", AutogramMimeType.APPLICATION_XML);
        var parameters = parameters(false);

        var result = AutogramDocument.build(document, EFormAttributes.build(parameters, true));

        assertTrue(result.getEFormAttributes().containerXmlns().contains("xmldatacontainer"));
    }

    private static void assertXdcValidationFails(String resource) throws IOException {
        var document = new InMemoryDocument(TestMethodSources.loadContent(resource), "test.xml",
                AutogramMimeType.XML_DATACONTAINER);
        var parameters = parameters(false);

        assertThrows(XMLValidationException.class,
                () -> AutogramDocument.build(document, EFormAttributes.build(parameters, true)));
    }

    private static SigningParameters parameters(boolean plainXmlEnabled) {
        return SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256, null, null,
                false, null, null, null, false, 800, plainXmlEnabled);
    }

    private static EFormAttributes explicitAttributes(DSSDocument document) {
        var parameters = parameters(true);
        return new EFormAttributes(IDENTIFIER, xsltTransformation, xsdSchema, XDC_XMLNS, null, null, false, null,
                AutogramMimeType.isAsice(document.getMimeType()), parameters.getPropertiesCanonicalization(),
                parameters.getDigestAlgorithm());
    }

    private static EFormAttributes attributes(String containerXmlns) {
        return new EFormAttributes(IDENTIFIER, xsltTransformation, xsdSchema, containerXmlns, null, null, false, null,
                false, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256);
    }
}