package digital.slovensko.autogram;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.XDCBuilder;
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

class XDCBuilderTests {
    // @Test
    // void testTransformsPlainHtmlWithoutAddingNamespaces() throws IOException {
    //     var transformation = new String(this.getClass().getResourceAsStream("general_agenda.xslt").readAllBytes());
    //     var xsdSchema = new String(this.getClass().getResourceAsStream("general_agenda.xsd").readAllBytes());

    //     var document = new InMemoryDocument(this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes(), "general_agenda.xml", MimeTypeEnum.XML);

    //     var params = SigningParameters.buildFromRequest(
    //         SignatureLevel.XAdES_BASELINE_B,
    //         ASiCContainerType.ASiC_E,
    //         "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
    //         SignaturePackaging.ENVELOPING,
    //         DigestAlgorithm.SHA256,
    //         false,
    //         CanonicalizationMethod.INCLUSIVE,
    //         CanonicalizationMethod.INCLUSIVE,
    //         CanonicalizationMethod.INCLUSIVE,
    //         xsdSchema,
    //         transformation,
    //         "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
    //         false, 800, false, document);

    //     var out = XDCBuilder.transform(params, document.getName(), EFormUtils.getXmlFromDocument(document));
    //     var transformed = new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

    //     var expected = new String(this.getClass().getResourceAsStream("general_agenda_xdc.xml").readAllBytes(), StandardCharsets.UTF_8);
    //     // couldn't find a way to compare XMLs without the newline at the end
    //     assertEquals(expected, transformed + "\n");
    // }
}
