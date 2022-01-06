package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.preprocessing.XDCTransformer;


public class SignatureUnit {
    private Document document;
    private SignatureParameters signatureParameters;
    private MimeType mimeType;

    public SignatureUnit(Document document, SignatureParameters signatureParameters, MimeType mimeType) {
        this.document = document;
        this.signatureParameters = signatureParameters;
        this.mimeType = mimeType;
    }

    public Document getDocument() {
        return document;
    }

    public SignatureParameters getSignatureParameters() {
        return signatureParameters;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public boolean isPDF() {
        return mimeType.is(MimeType.PDF);
    }

    public boolean isXML() {
        return mimeType.is(MimeType.XML);
    }

    public boolean isXDC() {
        return isXML() && signatureParameters.getContainer() != null;
    }

    public void transformToXDC() {
        var transformer = XDCTransformer.newInstance(signatureParameters);
        var transformedContent = transformer.transform(document.getContent(), XDCTransformer.Mode.IDEMPOTENT);
        document.setContent(transformedContent);
    }
}
