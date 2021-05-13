package com.octosign.whitelabel.communication.server.format;

import com.google.gson.Gson;

public class JsonFormat extends BodyFormat {

    public static final String MIME_TYPE = "application/json";

    @Override
    public <T> T from(String input, Class<T> inputClass) {
        return new Gson().fromJson(input, inputClass);
    }

    @Override
    public <T> String to(T input) {
        return new Gson().toJson(input);
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE + ";charset=UTF-8";
    }

}
