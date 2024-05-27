package digital.slovensko.autogram.core.eforms;

import org.junit.jupiter.api.Assertions;
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
}