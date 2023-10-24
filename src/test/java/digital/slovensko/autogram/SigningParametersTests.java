package digital.slovensko.autogram;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class SigningParametersTests {
    @Test
    void testInvalidTransformation() {
        var transformation = "invalid transformation";
        var params = SigningParameters.buildFromRequest(SignatureLevel.XAdES_BASELINE_B,
            ASiCContainerType.ASiC_E, null, SignaturePackaging.ENVELOPING,
            DigestAlgorithm.SHA256, false, CanonicalizationMethod.INCLUSIVE,
            CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE, null,
            transformation, "id1/asa", false, 800, false, null);


        var document = new InMemoryDocument(
            this.getClass().getResourceAsStream("crystal_test_data/rozhodnutie_X4564-2.xml"), "rozhodnutie_X4564-2.xml");

        var job = new SigningJob(document, params, mock(Responder.class));
        assertThrows(TransformationParsingErrorException.class, () -> {
            DocumentVisualizationBuilder.fromJob(job);
        });
    }
}
