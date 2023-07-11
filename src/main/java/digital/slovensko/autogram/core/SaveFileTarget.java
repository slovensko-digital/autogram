package digital.slovensko.autogram.core;

import java.io.File;
import java.util.List;

public class SaveFileTarget {

    public static File getTargetFromList(List<File> list) {
        if (list == null || list.isEmpty())
            return null;

        if (list.size() == 1)
            return getTargetForSingleFile(list);

        return getTargetForMultipleFiles(list);
    }

    private static File getTargetForSingleFile(List<File> list) {
        var file = list.get(0);
        var target = new File(file.getParentFile(), file.getName() + ".signed.pdf");
        return target;
    }

    private static File getTargetForMultipleFiles(List<File> list) {
        var file = list.get(0);
        var target = new File(file.getParentFile(), "signed");
        return target;
    }
}
