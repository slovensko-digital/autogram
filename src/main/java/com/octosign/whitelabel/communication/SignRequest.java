package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;

/**
 * Immutable Sign Request Info
 */
public class SignRequest {

    private Document document;
    private SignatureParameters parameters;
    private MimeType payloadMimeType;
    private String hmac;

    public SignRequest() {}

    public SignRequest(Document document, SignatureParameters parameters, MimeType payloadMimeType, String hmac) {
        this.document = document;
        this.parameters = parameters;
        this.payloadMimeType = payloadMimeType;
        this.hmac = hmac;
    }

    public Document getDocument() {
        return document;
    }

    public SignatureParameters getParameters() {
        return parameters;
    }

    public MimeType getPayloadMimeType() {
        return payloadMimeType;
    }

    public String getHmac() {
        return hmac;
    }

}
