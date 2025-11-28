package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SigningParametersTests {

    @Test
    public void testXDCValidationWithValidTransformationHash() throws Exception {
        var xdcContent = loadTestResource("digital/slovensko/autogram/general_agenda_xdc_indented.xml");
        var xdcDocument = new InMemoryDocument(xdcContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.XML_DATACONTAINER);

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
        var xdcContent = loadTestResource("digital/slovensko/autogram/fs_forms/d_fs792_772_xdc_xslt_digest.xml");
        var xdcDocument = new InMemoryDocument(xdcContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.XML_DATACONTAINER);

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
        var xdcContent = loadTestResource("digital/slovensko/autogram/fs_forms/d_fs792_772_xdc_xsd_digest.xml");
        var xdcDocument = new InMemoryDocument(xdcContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.XML_DATACONTAINER);

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
        var xdcContent = loadTestResource("digital/slovensko/autogram/wrong_schema_ga_xdc.xml");
        var xdcDocument = new InMemoryDocument(xdcContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.XML_DATACONTAINER);

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
        var xdcContent = loadTestResource("digital/slovensko/autogram/fs_forms/d_fs792_772_xdc_xslt_digest.xml");
        var xdcDocument = new InMemoryDocument(xdcContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.XML_DATACONTAINER);

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
        var xdcContent = loadTestResource("digital/slovensko/autogram/general_agenda_xdc_indented.xml");
        var xdcDocument = new InMemoryDocument(xdcContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.APPLICATION_XML);

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
        var xmlContent = loadTestResource("digital/slovensko/autogram/general_agenda.xml");
        var xmlDocument = new InMemoryDocument(xmlContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.APPLICATION_XML);

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
        var xmlContent = loadTestResource("digital/slovensko/autogram/general_agenda.xml");
        var xmlDocument = new InMemoryDocument(xmlContent.getBytes(StandardCharsets.UTF_8), "test.xml", AutogramMimeType.APPLICATION_XML);

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

    private String loadTestResource(String resourcePath) throws IOException {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(resourcePath);
        if (resource == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return Files.readString(Paths.get(resource.getPath()), StandardCharsets.UTF_8);
    }
}

