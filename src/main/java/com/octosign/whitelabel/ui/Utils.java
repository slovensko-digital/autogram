package com.octosign.whitelabel.ui;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.Base64;

public class Utils {
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isPresent(String value) {
        return !isNullOrBlank(value);
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> T first(Collection<T> collection) {
//        if (isNullOrEmpty(collection))
//            throw ;

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

    @SafeVarargs
    public static <T> T[] concat(T[]... args) {
        return (T[]) Stream.of(args)
                .map(Objects::requireNonNull)
                .flatMap(Stream::of)
                .toArray();
    }

    @SafeVarargs
    public static <T> List<T> toFlattenedList(T[]... args) {
        return Stream.of(args)
                .map(Objects::requireNonNull)
                .flatMap(Stream::of)
                .toList();
    }
}
