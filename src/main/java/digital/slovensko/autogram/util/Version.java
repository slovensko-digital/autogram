package digital.slovensko.autogram.util;

import java.util.stream.Stream;

public class Version implements Comparable<Version> {

    private int[] versionNumbers;

    public Version(int[] versionNumbers) {
        this.versionNumbers = versionNumbers;
    }

    public static Version createFromVersionString(String version) {
        version = version.replaceAll("^v", "");

        if (version.equals("")) {
            return new Version(new int[]{});
        }
        var versionNumbers =  Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
        return new Version(versionNumbers);
    }

    private int[] getVersionNumbers() {
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
