package digital.slovensko.autogram;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.XDCTransformer;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XDCTransformerTests {
    @Test
    void testTransformsPlainHtmlWithoutAddingNamespaces() throws IOException {
        var transformation =
                new String(this.getClass().getResourceAsStream("abc.xslt").readAllBytes());

        var document = new InMemoryDocument(this.getClass().getResourceAsStream("abc.xml"));

        var params =
                new SigningParameters(SignatureLevel.XAdES_BASELINE_B, ASiCContainerType.ASiC_E,
                        "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                        SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256, false,
                        CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE,
                        CanonicalizationMethod.INCLUSIVE, null, transformation, "id1/asa", false,
                        800, null);

        var out = XDCTransformer.buildFromSigningParameters(params, MimeTypeEnum.HTML).transform(document);
        var transformed = new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(-1, transformed.lastIndexOf(":p>"));
    }
}
