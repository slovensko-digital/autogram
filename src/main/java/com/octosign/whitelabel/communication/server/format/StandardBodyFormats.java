package com.octosign.whitelabel.communication.server.format;

import com.google.gson.Gson;

public enum StandardBodyFormats implements BodyFormat {
    JSON {
        static final String MIME_TYPE = "application/json;charset=utf-8";

        private final Gson gson = new Gson();

        @Override
        public <T> T from(String input, Class<T> inputClass) {
            return gson.fromJson(input, inputClass);
        }

        @Override
        public <T> String to(T input) {
            return gson.toJson(input);
        }

        @Override
        public String getMimeType() {
            return MIME_TYPE;
        }
    }
}
