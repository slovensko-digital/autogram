package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.errors.XMLValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class OrsrEFormResourcesTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://www.justice.gov.sk/Forms http://httpbin.org/get?ssrf=proof",
            "http://www.justice.gov.sk/Forms file:///etc/passwd",
            "http://www.justice.gov.sk/Forms http://evil.com/evil.xsd",
    })
    void testFindResourcesRejectsNonOrsrUrl(String schemaLocation) {
        var resources = new OrsrEFormResources(schemaLocation, null, null);
        Assertions.assertThrows(XMLValidationException.class, resources::findResources);
    }
}

