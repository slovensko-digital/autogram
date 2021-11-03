package com.octosign.whitelabel;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public enum ContentType {
    APPLICATION_ATOM_XML("application/atom+xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_SOAP_XML("application/soap+xml"),
    APPLICATION_SVG_XML("application/svg+xml"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    IMAGE_BMP("image/bmp"),
    IMAGE_GIF("image/gif"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_SVG("image/svg+xml"),
    IMAGE_TIFF("image/tiff"),
    IMAGE_WEBP("image/webp"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    WILDCARD("*"),
    CONTENT_TYPE_MAP(""),
    DEFAULT_TEXT(""),
    DEFAULT_BINARY("");

    private static final List<ContentType> utf8s = List.of(APPLICATION_JSON, APPLICATION_SOAP_XML);
    private static final List<ContentType> iso88591s = List.of(APPLICATION_ATOM_XML, APPLICATION_FORM_URLENCODED, APPLICATION_SVG_XML, APPLICATION_XHTML_XML, APPLICATION_XML, MULTIPART_FORM_DATA, TEXT_HTML, TEXT_PLAIN, TEXT_XML);
    private static final List<ContentType> nulls = List.of(APPLICATION_OCTET_STREAM, IMAGE_BMP, IMAGE_GIF, IMAGE_JPEG, IMAGE_PNG, IMAGE_SVG, IMAGE_TIFF, IMAGE_WEBP, WILDCARD, CONTENT_TYPE_MAP, DEFAULT_TEXT, DEFAULT_BINARY);

    private final String value;

    ContentType(String value) { this.value = value; }

    public static Charset getCharset(ContentType contentType) {
        if (utf8s.contains(contentType)) return UTF_8;
        if (iso88591s.contains(contentType)) return ISO_8859_1;
        if (nulls.contains(contentType)) return null;

        throw new IntegrationException(Code.MALFORMED_INPUT);
    }

    public static Charset getCharset(String contentType) {
        return getCharset(ContentType.valueOf(contentType));
    }

    public String getValue() { return value; }

    public enum CharsetMapper {
        UTF_8(StandardCharsets.UTF_8),
        ASCII(StandardCharsets.US_ASCII),
        ISO_8859_1(StandardCharsets.ISO_8859_1);

        private final Charset value;

        CharsetMapper(Charset value) { this.value = value; }

        public Charset getValue() { return value; }
    }
}
