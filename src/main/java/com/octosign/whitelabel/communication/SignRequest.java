package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;

/**
 * Immutable Sign Request Info
 */
public class SignRequest {

    private Document document;

    private SignatureParameters parameters;

    private String payloadMimeType;

    private String hmac;

    public SignRequest() {}

    public SignRequest(Document document, SignatureParameters parameters, String payloadMimeType, String hmac) {
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

    public String getPayloadMimeType() {
        return payloadMimeType;
    }

    public String getHmac() {
        return hmac;
    }

}
