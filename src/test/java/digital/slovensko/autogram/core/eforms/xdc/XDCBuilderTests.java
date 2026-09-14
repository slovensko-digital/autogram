package digital.slovensko.autogram.core.eforms.xdc;

import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.SigningInput;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import digital.slovensko.autogram.TestMethodSources;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XDCBuilderTests {
    @Test
    void testTransformsPlainHtmlWithoutAddingNamespaces() throws IOException {
        var transformation = new String(TestMethodSources.loadContent("general_agenda.xslt"), StandardCharsets.UTF_8);
        var xsdSchema = new String(TestMethodSources.loadContent("general_agenda.xsd"), StandardCharsets.UTF_8);

        var document = new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "general_agenda.xml", MimeTypeEnum.XML);

        var eFormAttributes = new EFormAttributes(
            "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
            transformation,
            xsdSchema,
            "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
            null,
            null,
            false,
            null,
            false,
            CanonicalizationMethod.INCLUSIVE,
            DigestAlgorithm.SHA256);
        var parameters = SigningParameters.buildParameters(
            SignatureLevel.XAdES_BASELINE_B,
            DigestAlgorithm.SHA256,
            ASiCContainerType.ASiC_E,
            SignaturePackaging.ENVELOPING,
            false,
            CanonicalizationMethod.INCLUSIVE,
            CanonicalizationMethod.INCLUSIVE,
            CanonicalizationMethod.INCLUSIVE,
            false,
            800,
            true);
        var params = SigningInput.fromDocument(AutogramDocument.build(document, eFormAttributes), parameters);

        var out = XDCBuilder.transform(params.getFirstDocument().getEFormAttributes(), params.getParameters().getPropertiesCanonicalization(),
            params.getParameters().getDigestAlgorithm(), document.getName(),
            EFormUtils.getXmlFromDocument(document));
        var transformed = new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

        var expected = new String(TestMethodSources.loadContent("general_agenda_xdc.xml"), StandardCharsets.UTF_8);

        // couldn't find a way to compare XMLs without the newline at the end
        // delete \n or \r\n from the end of the string
        expected = expected.replaceAll("\\r\\n$|\\n$", "");
        assertEquals(expected, transformed);
    }
}
