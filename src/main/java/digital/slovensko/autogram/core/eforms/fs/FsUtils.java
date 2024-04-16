package digital.slovensko.autogram.core.eforms.fs;

import java.util.regex.Pattern;

public abstract class FsUtils {
    public static String getFsFormIdFromFilename(String filename) {
        var matcher = Pattern.compile("^.+_fs(\\d{2,4}_\\d{2,4}).*\\.(xml|xdcf|asice|sce|)$").matcher(filename);

        if (!matcher.find())
            return null;

        return matcher.group(1);
    }
}
