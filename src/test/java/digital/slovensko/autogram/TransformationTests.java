package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import org.junit.jupiter.api.Test;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;

public class TransformationTests {

        Responder dummyResponder = new Responder() {
                @Override
                public void onDocumentSigned(SignedDocument signedDocument) {}

                @Override
                public void onDocumentSignFailed(AutogramException error) {
                        fail("Should not have thrown any exception", error);
                }
        };


        @Test
        void testSigningJobTransformToHtml() {
                try {
                        var transformation = new String(this.getClass().getResourceAsStream(
                                        "crystal_test_data/PovolenieZdravotnictvo.html.xslt")
                                        .readAllBytes());

                        var document = new InMemoryDocument(this.getClass().getResourceAsStream(
                                        "crystal_test_data/rozhodnutie_X4564-2.xml"),
                                        "rozhodnutie_X4564-2.xml");

                        var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
                                        ASiCContainerType.ASiC_E, null,
                                        SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256,
                                        false, CanonicalizationMethod.INCLUSIVE,
                                        CanonicalizationMethod.INCLUSIVE,
                                        CanonicalizationMethod.INCLUSIVE, null, transformation,
                                        "id1/asa", false, 800);

                        SigningJob job = new SigningJob(document, params, dummyResponder);


                        var xml = job.getDocumentAsHTML();
                        assertFalse(xml.isEmpty());
                } catch (Exception e) {
                        fail("Should not have thrown any exception", e);
                }
        }


        @Test
        void testSigningJobTransformFo() {
                try {
                        var transformation = new String(this.getClass().getResourceAsStream(
                                        "crystal_test_data/PovolenieZdravotnictvo.fo.xsl")
                                        .readAllBytes());

                        var document = new InMemoryDocument(this.getClass().getResourceAsStream(
                                        "crystal_test_data/rozhodnutie_X4564-2.xml"),
                                        "rozhodnutie_X4564-2.xml");

                        var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
                                        ASiCContainerType.ASiC_E, null,
                                        SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256,
                                        false, CanonicalizationMethod.INCLUSIVE,
                                        CanonicalizationMethod.INCLUSIVE,
                                        CanonicalizationMethod.INCLUSIVE, null, transformation,
                                        "id1/asa", false, 800);

                        SigningJob job = new SigningJob(document, params, dummyResponder);

                        var xml = job.getDocumentAsHTML();
                        assertFalse(xml.isEmpty());
                } catch (Exception e) {
                        fail("Should not have thrown any exception", e);
                }
        }

        @Test
        void testSigningJobTransformSb() {
                try {
                        var transformation = new String(this.getClass().getResourceAsStream(
                                        "crystal_test_data/PovolenieZdravotnictvo.sb.xslt")
                                        .readAllBytes());

                        var document = new InMemoryDocument(this.getClass().getResourceAsStream(
                                        "crystal_test_data/rozhodnutie_X4564-2.xml"),
                                        "rozhodnutie_X4564-2.xml");

                        var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
                                        ASiCContainerType.ASiC_E, null,
                                        SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256,
                                        false, CanonicalizationMethod.INCLUSIVE,
                                        CanonicalizationMethod.INCLUSIVE,
                                        CanonicalizationMethod.INCLUSIVE, null, transformation,
                                        "id1/asa", false, 800);

                        SigningJob job = new SigningJob(document, params, dummyResponder);

                        var xml = job.getDocumentAsHTML();
                        assertFalse(xml.isEmpty());
                } catch (Exception e) {
                        fail("Should not have thrown any exception", e);
                }
        }
}
