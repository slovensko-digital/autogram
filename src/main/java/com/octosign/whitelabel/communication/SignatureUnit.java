package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.preprocessing.XDCTransformer;

import java.nio.charset.StandardCharsets;

import static com.octosign.whitelabel.communication.SignatureParameters.Format.PADES;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

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

    public void setSignatureParameters(SignatureParameters signatureParameters) {
        this.signatureParameters = signatureParameters;
    }

    public byte[] getBinaryContent() {
        if (signatureParameters.getFormat() == PADES) {
            return decodeBase64(document.getContent());
        } else {
            return document.getContent().getBytes(StandardCharsets.UTF_8);
        }
    }

    public void transformToXDC() {
        var transformer = XDCTransformer.newInstance(signatureParameters);
        var transformedContent = transformer.transform(document.getContent(), XDCTransformer.Mode.IDEMPOTENT);

        document.setContent(transformedContent);
    }
}
