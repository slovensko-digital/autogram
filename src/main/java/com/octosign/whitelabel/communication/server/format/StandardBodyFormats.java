package com.octosign.whitelabel.communication.server.format;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.error_handling.IntegrationException;

public enum StandardBodyFormats implements BodyFormat {
    JSON {
        static final MimeType mimeType = MimeType.JSON_UTF8;

        private final static Gson gson;

        static {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(IntegrationException.class, new ExceptionSerializer());
            gson = gsonBuilder.create();
        }

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

        static class ExceptionSerializer implements JsonSerializer<IntegrationException> {
            @Override
            public JsonElement serialize(
                IntegrationException src,
                Type typeOfSrc,
                JsonSerializationContext context
            ) {
                JsonObject jsonObject = new JsonObject();

                var code = src.getCode();
                jsonObject.add("code", new JsonPrimitive(code.toString()));

                var message = src.getMessage();
                jsonObject.add("message", new JsonPrimitive(message != null ? message : "Unknown error"));

                var cause = src.getCause();
                if (cause != null) {
                    jsonObject.add("details", new JsonPrimitive(cause.getMessage()));
                }
                return jsonObject;
            }
        }
    }
}
