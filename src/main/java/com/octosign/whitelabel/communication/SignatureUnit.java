package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.preprocessing.XDCTransformer;

import static com.octosign.whitelabel.communication.MimeType.*;


public class SignatureUnit {
    private final Document document;
    private final SignatureParameters signatureParameters;
    private final MimeType mimeType;

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
        return mimeType.is(PDF);
    }

    public boolean isXML() {
        return mimeType.is(MimeType.XML);
    }

    public boolean isPlainOldXML() {
        return isXML() && signatureParameters.getContainer() == null;
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
