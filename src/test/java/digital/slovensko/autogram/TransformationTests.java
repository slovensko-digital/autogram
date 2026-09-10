package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.ParserConfigurationException;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.visualization.UnsupportedVisualization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.core.visualization.HTMLVisualization;
import digital.slovensko.autogram.core.visualization.Visualization;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TransformationTests {

        Responder dummyResponder = new Responder() {
                @Override
                public void onDocumentSigned(SignedDocument signedDocument) {
                }

                @Override
                public void onDocumentSignFailed(AutogramException error) {
                        fail("Should not have thrown any exception", error);
                }
        };

        @Test
        void testSigningJobTransformToHtml() throws IOException, ParserConfigurationException,
                SAXException {
                var transformation = new String(this.getClass().getResourceAsStream(
                                "crystal_test_data/PovolenieZdravotnictvo.html.xslt")
                        .readAllBytes());

                var schema = new String(this.getClass()
                        .getResourceAsStream(
                                "crystal_test_data/rozhodnutie_X4564-2.xsd")
                        .readAllBytes());

                var document = new InMemoryDocument(
                        this.getClass().getResourceAsStream(
                                "crystal_test_data/rozhodnutie_X4564-2.xml"),
                        "rozhodnutie_X4564-2.xml");

                var params = SigningParameters.prepareParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    CanonicalizationMethod.INCLUSIVE,
                    CanonicalizationMethod.INCLUSIVE,
                    CanonicalizationMethod.INCLUSIVE,
                    new EFormAttributes(
                        "id1/asa",
                        transformation,
                        schema,
                        "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                        null,
                        null,
                        false),
                    false,
                    null,
                    false,
                    800,
                    document,
                    null,
                    true);

                var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document)
                                .withEFormAttributes(params.eFormAttributes()),
                        params.signingParameters());
                SigningJob job = SigningJob.fromInput(input, dummyResponder);

                var visualizedDocument = DocumentVisualizationBuilder.fromJob(job, UserSettings.load());
                if (visualizedDocument instanceof HTMLVisualization d) {
                        var html = d.getDocument();
                        assertFalse(html.isEmpty());
                } else {
                        if (visualizedDocument != null)
                                fail("Expected HTMLVisualizedDocument but got"
                                        + visualizedDocument.getClass().getName());
                        fail("Expected HTMLVisualizedDocument but got null");
                }
        }

        // @Test
        // void testSigningJobTransformFo() throws IOException,
        // ParserConfigurationException,
        // SAXException, TransformerException {
        // var transformation = new String(this.getClass()
        // .getResourceAsStream(
        // "crystal_test_data/PovolenieZdravotnictvo.fo.xsl")
        // .readAllBytes());

        // var document = new InMemoryDocument(
        // this.getClass().getResourceAsStream(
        // "crystal_test_data/rozhodnutie_X4564-2.xml"),
        // "rozhodnutie_X4564-2.xml");

        // var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
        // ASiCContainerType.ASiC_E, null, SignaturePackaging.ENVELOPING,
        // DigestAlgorithm.SHA256, false, CanonicalizationMethod.INCLUSIVE,
        // CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE,
        // null, transformation, "id1/asa", false, 800);

        // SigningJob job = new SigningJob(document, params, dummyResponder);

        // var xml = DocumentVisualization.fromJob(job).getVisualizedDocument();
        // assertFalse(xml.isEmpty());
        // }

        @Test
        void testSigningJobTransformSb() throws IOException, ParserConfigurationException,
                SAXException {
                var transformation = new String(this.getClass()
                        .getResourceAsStream(
                                "crystal_test_data/PovolenieZdravotnictvo.sb.xslt")
                        .readAllBytes());

                var schema = new String(this.getClass()
                        .getResourceAsStream(
                                "crystal_test_data/rozhodnutie_X4564-2.xsd")
                        .readAllBytes());

                var document = new InMemoryDocument(
                        this.getClass().getResourceAsStream(
                                "crystal_test_data/rozhodnutie_X4564-2.xml"),
                        "rozhodnutie_X4564-2.xml");

                var params = SigningParameters.prepareParameters(
                    SignatureLevel.XAdES_BASELINE_B,
                    DigestAlgorithm.SHA256,
                    ASiCContainerType.ASiC_E,
                    SignaturePackaging.ENVELOPING,
                    false,
                    CanonicalizationMethod.INCLUSIVE,
                    CanonicalizationMethod.INCLUSIVE,
                    CanonicalizationMethod.INCLUSIVE,
                    new EFormAttributes(
                        "id1/asa",
                        transformation,
                        schema,
                        "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                        null,
                        null,
                        false),
                    false,
                    null,
                    false,
                    800,
                    document,
                    null,
                    true);

                var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document)
                                .withEFormAttributes(params.eFormAttributes()),
                        params.signingParameters());
                SigningJob job = SigningJob.fromInput(input, dummyResponder);

                var visualizedDocument = DocumentVisualizationBuilder.fromJob(job, UserSettings.load());
                if (visualizedDocument instanceof HTMLVisualization d) {
                        var html = d.getDocument();
                        assertFalse(html.isEmpty());
                } else {
                        fail("Expected HTMLVisualizedDocument");
                }
        }

        @ParameterizedTest
        @MethodSource("digital.slovensko.autogram.TestMethodSources#unsetXdcfMimetypeProvider")
        void testXdcfVisualizationIsNotUnsupported(InMemoryDocument document)
                throws IOException, ParserConfigurationException, SAXException {
                var params = SigningParameters.buildForASiCWithXAdES(document, false, false, null, false);
                var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document), params);
                var job = SigningJob.fromInput(input, dummyResponder);

                var visualization = DocumentVisualizationBuilder.fromJob(job, UserSettings.load());

                Assertions.assertFalse(visualization instanceof UnsupportedVisualization);
        }

        @Test
        void testMultiDocumentVisualizationUsesSelectedDocumentMetadata()
                throws IOException, ParserConfigurationException, SAXException {
                var firstDocument = AutogramDocument.fromContent("first".getBytes(StandardCharsets.UTF_8), "first.txt",
                        MimeTypeEnum.TEXT);
                var secondDocument = AutogramDocument.fromContent(
                        this.getClass().getResourceAsStream("general_agenda.xdcf").readAllBytes(),
                        "generalAgendaInlineXdcfBinary.xdcf", MimeTypeEnum.BINARY);
                var params = SigningParameters.buildForASiCWithXAdES(firstDocument.toDssDocument(), false, false, null,
                        false);
                var job = SigningJob.fromInput(
                        SigningInput.of(List.of(firstDocument, secondDocument), params), dummyResponder);

                Visualization visualization = DocumentVisualizationBuilder.fromDocument(job,
                        job.getAutogramDocuments().get(1),
                        UserSettings.load());

                Assertions.assertFalse(visualization instanceof UnsupportedVisualization);
        }
}
