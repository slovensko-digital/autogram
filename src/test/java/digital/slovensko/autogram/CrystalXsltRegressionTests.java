package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.XDCTransformer;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;

class CrystalXsltRegressionTests {
        @Test
        void testTransformToHtml() throws IOException {
                var transformation = new String(this.getClass().getResourceAsStream(
                                "crystal_test_data/PovolenieZdravotnictvo.html.xslt")
                                .readAllBytes());

                var document = new InMemoryDocument(this.getClass()
                                .getResourceAsStream("crystal_test_data/rozhodnutie_X4564-2.xml"));

                var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
                                ASiCContainerType.ASiC_E,
                                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                                SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256, false,
                                CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE,
                                CanonicalizationMethod.INCLUSIVE, null, transformation, "id1/asa",
                                false, 800);

                var out = XDCTransformer.buildFromSigningParameters(params).transform(document);

                var transformed =
                                new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

                var expected = new String(this.getClass()
                                .getResourceAsStream("crystal_test_data/rozhodnutie_X4564-2.xml")
                                .readAllBytes(), StandardCharsets.UTF_8);


                var myDiffSimilar = DiffBuilder.compare(transformed).withTest(expected)
                                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                                .checkForSimilar()
                                .build();
                // myDiffSimilar.getDifferences().forEach(d -> System.out.println(d.toString()));
                // assertFalse(myDiffSimilar.hasDifferences(),
                //                 "XML similar " + myDiffSimilar.toString());


                System.out.println(expected);
        }

        @Test
        void testSigningJob(){
                
        }
}
