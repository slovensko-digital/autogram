package com.octosign.whitelabel.communication;

import java.util.Arrays;
import java.util.List;

public class MimeType {
    private String type;
    private List<String> parameterEntries;
    private boolean isBase64;

    public MimeType(String type, List<String> parameterEntries, boolean isBase64) {
        this.type = type;
        this.parameterEntries = parameterEntries;
        this.isBase64 = isBase64;
    }

    public static MimeType parse(String rawData) {
        // MIME type can have optional params separated by ;, e.g. some/type;base64
        var parts = rawData.replaceAll("\\s","").toLowerCase().split(";");

        var type = parts[0];
        var parameterList= Arrays.stream(parts).skip(1).toList();
        var isBase64 = parameterList.stream().anyMatch(e -> e.contains("base64"));

        return new MimeType(type, parameterList, isBase64);
    }

    public String getType() { return type; }
    public List<String> getParameterEntries() { return parameterEntries; }
    public boolean isBase64() { return isBase64; }
}
