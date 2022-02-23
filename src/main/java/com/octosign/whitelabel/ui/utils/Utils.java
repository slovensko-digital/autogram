package com.octosign.whitelabel.ui.utils;

import com.google.common.io.Files;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.Base64;

public class Utils {
    public static boolean not(boolean value) {
        return !value;
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

    public static String encodeBase64SafeChars(byte[] value) {
        byte[] decoded = Base64.getUrlEncoder().encode(value);

        return new String(decoded, StandardCharsets.UTF_8);
    }

    public static boolean areEqual(File file1, File file2) {
        try (InputStream is1 = new FileInputStream(file1);
             InputStream is2 = new FileInputStream(file2)) {

            if (Files.equal(file1, file2)) {
                return Arrays.equals(is1.readAllBytes(), is2.readAllBytes());
            }
        } catch (IOException e) {
            System.out.println("Unable to open and/or compare these two files.");
            e.printStackTrace();
        }

        return false;
    }

    public static boolean fileExists(Path path) {
        return path != null && path.toFile().exists() && path.toFile().canRead();
    }

    public static boolean fileExists(File file) {
        return file != null && file.exists();
    }

    public static byte[] readBytes(Path path) {
        try (var stream = new FileInputStream(path.toFile())) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read from file: %s", path));
        }
    }

    public static byte[] readBytes(File file) {
        return readBytes(file.toPath());
    }

    private static HttpExchange currentExchange;

    public static void setExchange(HttpExchange e) {
        currentExchange = e;
    }

    public static HttpExchange getExchange() {
        return currentExchange;
    }
}
