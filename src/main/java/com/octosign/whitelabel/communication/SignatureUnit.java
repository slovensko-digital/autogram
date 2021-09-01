package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;

public class SignatureUnit {
    private Document document;
    private SignatureParameters signatureParameters;

    public SignatureUnit(Document document, SignatureParameters signatureParameters) {
        this.document = document;
        this.signatureParameters = signatureParameters;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public SignatureParameters getSignatureParameters() {
        return signatureParameters;
    }

    public void setSignatureParameters(SignatureParameters signatureParameters) { this.signatureParameters = signatureParameters; }

}
