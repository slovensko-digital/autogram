package com.octosign.whitelabel.ui.utils;

import com.sun.net.httpserver.HttpExchange;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

public class Utils {
    public static String normalized(String value) {
        return value.strip().trim().toLowerCase(Locale.ROOT);
    }

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
        if (value == null)
             return null;
        return encodeBase64(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeBase64(byte[] value) {
        if (value == null)
            return null;

        byte[] encoded = Base64.getEncoder().encode(value);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static byte[] encodeBase64ToByteArr(byte[] value) {
        return Base64.getEncoder().encode(value);
    }

    public static byte[] decodeBase64ToByteArr(String value) {
        if (isNullOrBlank(value))
            return null;

        return Base64.getDecoder().decode(value);
    }

    public static String decodeBase64(String value) {
        byte[] decoded = decodeBase64ToByteArr(value);
        if (value == null || decoded == null)
            return null;

        return new String(decoded, StandardCharsets.UTF_8);
    }

    private static HttpExchange currentExchange;

    public static void setExchange(HttpExchange e) {
        currentExchange = e;
    }

    public static HttpExchange getExchange() {
        return currentExchange;
    }
}
