package com.octosign.whitelabel.communication.server.format;

import com.google.gson.*;
import com.octosign.whitelabel.communication.*;
import com.octosign.whitelabel.communication.dto.*;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.error_handling.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static com.octosign.whitelabel.ui.utils.Utils.*;
import static java.nio.charset.StandardCharsets.*;

public enum StandardBodyFormats implements BodyFormat {
    JSON {
        static final MimeType mimeType = MimeType.JSON_UTF8;

        private final static Gson gson;

        static {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ErrorData.class, new ErrorDataSerializer());
            gsonBuilder.registerTypeAdapter(MimeType.class, new MimeTypeDeserializer());
            gsonBuilder.registerTypeAdapter(SignRequest.class, new SignRequestDeserializer());
            gsonBuilder.registerTypeAdapter(SignedData.class, new SignedDataSerializer());
            gson = gsonBuilder.serializeNulls().create();
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

        static class ErrorDataSerializer implements JsonSerializer<ErrorData> {
            @Override
            public JsonElement serialize(ErrorData src, Type type, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("code", new JsonPrimitive(src.httpCode()));

                var message = src.message();
                jsonObject.add("message", new JsonPrimitive(message != null ? message : "Unknown error"));

                return jsonObject;
            }
        }

        static class SignedDataSerializer implements JsonSerializer<SignedData> {
            @Override
            public JsonElement serialize(SignedData data, Type type, JsonSerializationContext context) {
                JsonObject jsonDocument = new JsonObject();
                Document document = data.document();

                boolean requireBase64Encoding = data.mimeType().isBase64() || !data.stringifyable();
                byte[] binaryContent = document.getContent();
                if (requireBase64Encoding)
                    binaryContent = encodeBase64ToByteArr(binaryContent);

                var content = new String(binaryContent, StandardCharsets.UTF_8);
                jsonDocument.add("content", new JsonPrimitive(content));

                var id = document.getId();
                var title = document.getTitle();
                var legalEffect = document.getLegalEffect();

                if (isPresent(id))
                    jsonDocument.add("id", new JsonPrimitive(id));
                if (isPresent(title))
                    jsonDocument.add("title", new JsonPrimitive(title));
                if (isPresent(legalEffect))
                    jsonDocument.add("legalEffect", new JsonPrimitive(legalEffect));

                return jsonDocument;
            }
        }

        static class SignRequestDeserializer implements JsonDeserializer<SignRequest> {
            @Override
            public SignRequest deserialize(JsonElement jElement, Type type, JsonDeserializationContext context) {
                JsonObject signRequest = jElement.getAsJsonObject();

                String hmac = getOptional("hmac", signRequest);
                MimeType payloadType = context.deserialize(signRequest.get("payloadMimeType"), MimeType.class);

                JsonObject jDocument = signRequest.get("document").getAsJsonObject();
                String id = getOptional("id", jDocument);
                String title = getOptional("title", jDocument);
                String legalEffect = getOptional("legalEffect", jDocument);
                String tempContent = jDocument.get("content").getAsString();
                byte[] content;
                if (payloadType.isBase64()) {
                    content = decodeBase64ToByteArr(tempContent);
                } else {
                    content = tempContent.getBytes(UTF_8);
                }

                JsonObject params = signRequest.get("parameters").getAsJsonObject();
                Format format = context.deserialize(params.get("format"), Format.class);
                Level level = context.deserialize(params.get("level"), Level.class);
                MimeType fileMimeType = context.deserialize(params.get("fileMimeType"), MimeType.class);
                Container container = context.deserialize(params.get("container"), Container.class);
                String containerFilename = getOptional("containerFilename", params);
                String containerXmlns = getOptional("containerXmlns", params);
                String identifier = getOptional("identifier", params);
                Packaging packaging = context.deserialize(params.get("packaging"), Packaging.class);
                DigestAlgorithm digestAlgorithm = context.deserialize(params.get("digestAlgorithm"), DigestAlgorithm.class);
                Boolean en319132 = isNullValue("en319132", params) ? null : params.get("en319132").getAsBoolean();
                CanonicalizationMethod infoCanonicalization = context.deserialize(params.get("infoCanonicalization"), CanonicalizationMethod.class);
                CanonicalizationMethod propertiesCanonicalization = context.deserialize(params.get("propertiesCanonicalization"), CanonicalizationMethod.class);
                CanonicalizationMethod keyInfoCanonicalization = context.deserialize(params.get("keyInfoCanonicalization"), CanonicalizationMethod.class);
                String signaturePolicyId = getOptional("signaturePolicyId", params);
                String signaturePolicyContent = getOptional("signaturePolicyContent", params);
                String schema = getOptional("schema", params);
                String transformation = getOptional("transformation", params);
                MimeType transformationOutputMimeType = context.deserialize(params.get("transformationOutputMimeType"), MimeType.class);

                if (payloadType.isBase64()) {
                    schema = decodeBase64(schema);
                    transformation = decodeBase64(transformation);
                }

                Document document = new Document(id, title, content, legalEffect);
                SignatureParameters signatureParameters = new SignatureParameters(format, level, fileMimeType, container, containerFilename, containerXmlns, identifier, packaging, digestAlgorithm, en319132, infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization, signaturePolicyId, signaturePolicyContent, schema, transformation, transformationOutputMimeType);

                return new SignRequest(document, signatureParameters, payloadType, hmac);
            }

            private String getOptional(String key, JsonObject source) {
                if (isNullValue(key, source))
                    return null;
                else
                    return source.get(key).getAsString();
            }

            private boolean isNullValue(String key, JsonObject source) {
                return source == null || source.isJsonNull() || source.get(key) == null || source.get(key).isJsonNull();
            }
        }

        static class MimeTypeDeserializer implements JsonDeserializer<MimeType> {
            @Override
            public MimeType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                if (jsonElement == null || jsonElement.isJsonNull())
                    throw new IntegrationException(Code.MISSING_INPUT);

                try {
                    return MimeType.parse(jsonElement.getAsString());
                } catch (MalformedMimetypeException e) {
                    throw new IntegrationException(Code.MALFORMED_MIMETYPE, e);
                }
            }
        }
    }
}
