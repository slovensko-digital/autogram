package digital.slovensko.autogram.core.eforms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class EFormUtilsTests {
    @ParameterizedTest
    @CsvSource({
            "dic_fs792_772_exte.xml,792_772",
            "dic_fs792_772_.xml,792_772",
            "dic_fs792_772.xml,792_772",
            "dic_fs792_772exte.xml,792_772",
            "dic2120515056_fs792_772_indented.xml,792_772",
            "dic2120515056_fs792_772indented.xml,792_772",
            "dic2120515056_fs792_772.xml,792_772",
            "dic_fs2682_712__idk.xml,2682_712",
            "dic_fs2682_712idk.xml,2682_712",
            "dic_fs2682_712_idk.xml,2682_712",
            "dic2120515056_fs792_772__1__0.xml,792_772",
            "invalid.xml,"
    })
    void testGetFsFormIdFromFilename(String filename, String expected) {
        Assertions.assertEquals(expected, EFormUtils.getFsFormIdFromFilename(filename));
    }

    @ParameterizedTest
    @CsvSource({
            "792_772,792_772"
    })
    void testTranslateFsFormId(String fsFormId, String expected) {
        Assertions.assertEquals(expected, EFormUtils.translateFsFormId(fsFormId));
    }

    @ParameterizedTest
    @CsvSource({
            "http://www.justice.gov.sk/Forms,true",
            "http://www.justice.gov.sk/Forms http://eformulare.justice.sk/path/form.xsd,true",
            "http://eformulare.justice.sk/form,true",
            "http://evil.com/form http://www.justice.gov.sk/form.xsd,false",
            "http://evil.com/?x=justice.gov.sk/Forms,false",
            "http://httpbin.org/forms,false",
    })
    void testIsOrsrUri(String uri, boolean expected) {
        Assertions.assertEquals(expected, EFormUtils.isOrsrUri(uri));
    }

    @Test
    void testIsOrsrUriReturnsFalseForNull() {
        Assertions.assertFalse(EFormUtils.isOrsrUri(null));
    }
}