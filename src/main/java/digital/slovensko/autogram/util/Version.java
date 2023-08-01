package digital.slovensko.autogram.util;

import java.util.stream.Stream;

public class Version implements Comparable<Version> {

    private String version;
    private int[] versionNumbers;

    public Version(String version) {
        this.version = version;
        this.versionNumbers = parseVersion(version.replaceAll("^v", ""));
    }

    private int[] parseVersion(String version) {
        if (version.equals("")) {
            return new int[]{};
        }
        return Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
    }

    public String getVersion() {
        return version;
    }

    public int[] getVersionNumbers() {
        return versionNumbers;
    }

    @Override
    public int compareTo(Version o) {
        var otherNumbers = o.getVersionNumbers();
        for (int i = 0; i < Math.min(versionNumbers.length, otherNumbers.length); i++) {
            var diff = versionNumbers[i] - otherNumbers[i];
            if (diff != 0) {
                return diff;
            }
        }
        return versionNumbers.length - otherNumbers.length;
    }

}
