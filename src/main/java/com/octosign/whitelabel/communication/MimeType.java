package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.error_handling.MalformedMimetypeException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;
import static java.util.stream.Collectors.toMap;

/**
 * MIME type representing type of the value
 *
 * See https://tools.ietf.org/html/rfc7231#section-3.1.1.1
 *
 * Inspired by Spring Framework's https://docs.spring.io/spring-framework/docs/3.0.x/javadoc-api/org/springframework/http/MediaType.html
 */
public record MimeType(String type, String subType, Map<String, String> parameters) {
    public final static MimeType ANY = new MimeType("*", "*");
    public final static MimeType JSON = new MimeType("application", "json");
    public final static MimeType JSON_UTF8 = new MimeType("application", "json", Map.of("charset", "UTF-8"));
    public final static MimeType HTML = new MimeType("text", "html");
    public final static MimeType PLAIN = new MimeType("text", "plain");
    public final static MimeType PDF = new MimeType("application", "pdf");
    public final static MimeType YAML = new MimeType("text", "yaml");
    public final static MimeType XML = new MimeType("application", "xml");

    public MimeType(String type, String subType) {
        this(type, subType, new HashMap<>());
    }

    public MimeType(String type, String subType, Map<String, String> parameters) {
        this.type = type;
        this.subType = subType;
        this.parameters = parameters;
    }

    public static MimeType parse(String rawData) throws MalformedMimetypeException {
        // MIME type can have optional params separated by ;, e.g. some/type;base64
        var parts = rawData.replaceAll("\\s", "").toLowerCase().split(";");

        var types = parts[0].split("/");
        if (types.length != 2) {
            throw new MalformedMimetypeException("Unsupported MIME type: " + rawData);
        }

        if (parts.length == 1) {
            return new MimeType(types[0], types[1]);
        }

        var parameters = Arrays.stream(parts)
                               .skip(1)
                               .map(s -> s.split("="))
                               .collect(toMap(
                                       param -> param[0],
                                       param -> (param.length > 1) ? param[1] : "")
                               );

        parameters.replaceAll((k, v) -> ("".equals(v)) ? null : v);
        return new MimeType(types[0], types[1], parameters);
    }

    public boolean isBase64() {
        return parameters.containsKey("base64");
    }

    public boolean equalsTypeSubtype(MimeType other) {
        if (this == other) {
            return true;
        }

        if (!this.type.equalsIgnoreCase(other.type)) {
            return false;
        }

        if (!this.subType.equalsIgnoreCase(other.subType)) {
            return false;
        }

        return true;
    }

    public boolean is(MimeType other) {
        if (other == null)
            return false;

        return equalsTypeSubtype(other);
    }

    @Override
    public boolean equals(Object obj) {
        var other = (MimeType) obj;
        if (this == other) {
            return true;
        }

        if (!equalsTypeSubtype(other)) {
            return false;
        }

        if (!this.parameters.equals(other.parameters)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        var firstPart = this.type + "/" + this.subType;

        if (parameters.size() == 0) {
            return firstPart;
        }

        var secondPart = parameters.entrySet().stream()
                                   .map(e -> (e.getValue() == null) ? e.getKey() : e.getKey() + "=" + e.getValue())
                                   .collect(Collectors.joining("; "));

        return firstPart + "; " + secondPart;
    }

    public eu.europa.esig.dss.model.MimeType toDSSMimeType() {
        return eu.europa.esig.dss.model.MimeType.fromMimeTypeString(this.toString());
    }
}
