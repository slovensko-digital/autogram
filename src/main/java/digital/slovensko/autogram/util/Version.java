package digital.slovensko.autogram.util;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Version implements Comparable<Version> {

    private int[] versionNumbers;
    private boolean dev;

    public Version(int[] versionNumbers) {
        this.versionNumbers = versionNumbers;
    }

    public static Version createFromVersionString(String version) {
        version = version.replaceAll("^v", "");

        if (version.equals("")) {
            return new Version(new int[]{});
        }

        if (version.equals("dev")) {
            return createDevVersion();
        }

        var versionNumbers = Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
        return new Version(versionNumbers);
    }

    private static Version createDevVersion() {
        var v = new Version(new int[]{});
        v.dev = true;
        return v;
    }


    public String toString() {
        return IntStream.of(versionNumbers).mapToObj(Integer::toString).collect(Collectors.joining("."));
    }


    public boolean isDev() {
        return dev;
    }

    @Override
    public int compareTo(Version o) {
        if (dev && o.dev) {
            return 0;
        }

        if (dev) {
            return 1;
        }

        if (o.dev) {
            return -1;
        }

        var otherNumbers = o.getVersionNumbers();
        for (int i = 0; i < Math.min(versionNumbers.length, otherNumbers.length); i++) {
            var diff = versionNumbers[i] - otherNumbers[i];
            if (diff != 0) {
                return diff;
            }
        }
        return versionNumbers.length - otherNumbers.length;
    }


    private int[] getVersionNumbers() {
        return versionNumbers;
    }

}
