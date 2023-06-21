package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;

public class SigningParametersTests {

    @Test()
    void testInvalidTransformation() {

        var transformation = "invalid transformation";
        var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
                ASiCContainerType.ASiC_E, null, SignaturePackaging.ENVELOPING,
                DigestAlgorithm.SHA256, false, CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE, null,
                transformation, "id1/asa", false, 800);

        var document = new InMemoryDocument(
                this.getClass().getResourceAsStream(
                        "crystal_test_data/rozhodnutie_X4564-2.xml"),
                "rozhodnutie_X4564-2.xml");
        var v =  DocumentVisualizationBuilder.fromDocumentAndParams(document, params).build();
        assertEquals(v.getError().getClass(), SAXParseException.class);
    }
}
