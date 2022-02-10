package com.octosign.whitelabel.communication.document;

import java.nio.charset.StandardCharsets;

public class ForbiddenTypeDocument extends Document {
    public ForbiddenTypeDocument(Document document) {
        super(document);
    }

    @Override
    public byte[] getContent() {
        return "".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentString() {
        return "";
    }

    @Override
    public String toString() {
        return "NOT_ALLOWED";
    }
}
