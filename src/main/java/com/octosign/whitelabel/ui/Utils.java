package com.octosign.whitelabel.ui;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Utils {
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
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
