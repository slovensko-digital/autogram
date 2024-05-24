package digital.slovensko.autogram.core.eforms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import eu.europa.esig.dss.model.DSSDocument;

public class XDCValidatorTests {
    @ParameterizedTest
    @MethodSource({"digital.slovensko.autogram.TestMethodSources#xdcDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#xdcDocumentsWithXmlMimetypeProvider"})
    void testReturnsTrueForAllXDCsRegerdlessOfMimeType(DSSDocument document) {
        Assertions.assertTrue(XDCValidator.isXDCContent(document));
    }

    @ParameterizedTest
    @MethodSource({"digital.slovensko.autogram.TestMethodSources#nonXdcXmlDocumentsProvider"})
    void testReturnsFalseForAllNonXDCsRegerdlessOfMimeType(DSSDocument document) {
        Assertions.assertFalse(XDCValidator.isXDCContent(document));
    }
}
