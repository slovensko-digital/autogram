package com.octosign.whitelabel.communication;

import java.util.HashMap;
import java.util.Map;

import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.INCLUSIVE;
import static com.octosign.whitelabel.communication.SignatureParameters.Container.ASICE;
import static com.octosign.whitelabel.communication.SignatureParameters.DigestAlgorithm.SHA256;
import static com.octosign.whitelabel.communication.SignatureParameters.Format.PADES;
import static com.octosign.whitelabel.communication.SignatureParameters.Format.XADES;
import static com.octosign.whitelabel.communication.SignatureParameters.Level.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Packaging.ENVELOPED;
import static java.util.Arrays.stream;

public enum Configuration {
    XADES_SK("xades"),
    PADES_SK("pades");

    private static final Map<Configuration, SignatureParameters> mapping;

    static {
        mapping = new HashMap<>();
        mapping.put(XADES_SK, new SignatureParameters(null, XADES, BASELINE_B, "application/lor.ip.xmldatacontainer+xml", ASICE, ENVELOPED, SHA256, false, INCLUSIVE, INCLUSIVE, INCLUSIVE, null,null,null, null,null, null, null));
        mapping.put(PADES_SK, new SignatureParameters(null, PADES, BASELINE_B, null, null, ENVELOPED, SHA256, false, null, null, null, null, null, null, null, null, null, null));
    }

    public static Configuration from(String templateId) {
        return stream(values())
                .filter(template -> template.identifier.equalsIgnoreCase(templateId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public static SignatureParameters resolve(String templateId) { return from(templateId).parameters(); }

    public SignatureParameters parameters() { return mapping.get(this); }

    private final String identifier;

    Configuration(String identifier) { this.identifier = identifier; }
}
