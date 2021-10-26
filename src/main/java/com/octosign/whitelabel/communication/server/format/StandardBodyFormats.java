package com.octosign.whitelabel.communication.server.format;

import com.google.gson.Gson;
import com.octosign.whitelabel.communication.MimeType;

public enum StandardBodyFormats implements BodyFormat {
    JSON {
        static final MimeType mimeType = MimeType.JSON_UTF8;

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
        public MimeType getMimeType() {
            return mimeType;
        }
    }
}
