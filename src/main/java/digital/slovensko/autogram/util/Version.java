package digital.slovensko.autogram.util;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Version implements Comparable<Version> {

    private final int[] versionNumbers;
    private final boolean dev;

    public Version(int[] versionNumbers) {
        this.versionNumbers = versionNumbers;
        this.dev = false;
    }

    /**
     * Dev version constructor
     */
    public Version() {
        this.versionNumbers = new int[]{};
        this.dev = true;
    }

    public static Version createFromVersionString(String version) {
        version = version.replaceAll("^v", "");

        if (version.equals("")) {
            return new Version(new int[]{});
        }

        if (version.equals("dev")) {
            return new Version();
        }

        var versionNumbers = Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
        return new Version(versionNumbers);
    }


    public String toString() {
        if (dev) {
            return "dev";
        }
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
