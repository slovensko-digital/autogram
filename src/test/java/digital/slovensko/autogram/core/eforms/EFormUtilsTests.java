package digital.slovensko.autogram.core.eforms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class EFormUtilsTests {
    @ParameterizedTest
    @CsvSource({
            "dic_fsDPFOBv23__exte.xml,DPFOBv23",
            "dic_fsDPFOBv23__.xml,DPFOBv23",
            "dic_fsDPFOBv23__.xml,DPFOBv23",
            "dic_fsDPFOBv23__1__0exte.xml,DPFOBv23__1__0",
            "dic2120515056_fsDPFOBv23__1__0_indented.xml,DPFOBv23__1__0",
            "dic2120515056_fsDPFOBv23__1__0indented.xml,DPFOBv23__1__0",
            "dic2120515056_fsDPFOBv23__1__0.xml,DPFOBv23__1__0",
            "dic_fsV2P_v21__idk.xml,V2P_v21",
            "dic_fsV2P_v21__2__3idk.xml,V2P_v21__2__3",
            "dic_fsV2P_v21__2__3_idk.xml,V2P_v21__2__3",
            "dic_fsDPFOBv23.xml,",
            "dic_fsDPFOBv23_.xml,",
            "dic2120515056_fsDPFOBv23___1__0.xml,DPFOBv23",
            "invalid.xml,"
    })
    void testGetFsFormIdFromFilename(String filename, String expected) {
        Assertions.assertEquals(expected, EFormUtils.getFsFormIdFromFilename(filename));
    }

        @ParameterizedTest
    @CsvSource({
            "DPFOBv23,DPFOBv23/1.0",
            "DPFOBv23/1.0,DPFOBv23/1.0",
            "DPFOBv23/2.3,DPFOBv23/2.3",
            "DPFOBv23__2__3,DPFOBv23/2.3",
    })
    void testTranslateFsFormId(String fsFormId, String expected) {
        Assertions.assertEquals(expected, EFormUtils.translateFsFormId(fsFormId));
    }
}