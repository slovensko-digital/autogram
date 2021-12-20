package com.octosign.whitelabel.ui;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.Base64;

public class Utils {
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isPresent(String value) {
        return !isNullOrBlank(value);
    }

    /**
     * Returns first item of any collection.
     * Syntactic sugar - improved readability, compared to eg. <code>collection.get(0)</code>
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> T first(Collection<T> collection) {
        return collection.stream().findFirst().orElseThrow(() -> new RuntimeException("Collection is null or has no data!"));
    }

    public static String encodeBase64(String value) {
        if (isNullOrBlank(value))
            return null;

        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64(String value) {
        if (isNullOrBlank(value))
            return null;

        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public static String parseExtension(String filename) {
        var extension = "";

        int index = filename.lastIndexOf('.');
        if (index > 0) {
            extension = filename.substring(index + 1);
        }

        return extension;
    }

    @SafeVarargs
    public static <T> T[] concat(T[]... args) {
        return (T[]) Stream.of(args)
                .map(Objects::requireNonNull)
                .flatMap(Stream::of)
                .toArray();
    }

}
