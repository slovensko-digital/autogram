package com.octosign.whitelabel.communication;

import com.google.common.collect.ImmutableMap;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.AbstractSerializableSignatureParameters;
import eu.europa.esig.dss.model.SerializableSignatureParameters;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.util.Map;

import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.INCLUSIVE;
import static com.octosign.whitelabel.communication.SignatureParameters.Container.ASICE;
import static com.octosign.whitelabel.communication.SignatureParameters.Container.ASICS;
import static com.octosign.whitelabel.communication.SignatureParameters.DigestAlgorithm.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Format.PADES;
import static com.octosign.whitelabel.communication.SignatureParameters.Format.XADES;
import static com.octosign.whitelabel.communication.SignatureParameters.Level.BASELINE_B;
import static com.octosign.whitelabel.communication.SignatureParameters.Packaging.ENVELOPED;
import static com.octosign.whitelabel.communication.SignatureParameters.Packaging.ENVELOPING;
import static eu.europa.esig.dss.enumerations.SignatureLevel.PAdES_BASELINE_B;
import static eu.europa.esig.dss.enumerations.SignatureLevel.XAdES_BASELINE_B;

public class SignatureParameterMapper {
    private static final Map<SignatureParameters.Level, Map<SignatureParameters.Format, SignatureLevel>> signatureLevelMapping =
        ImmutableMap.of(
            BASELINE_B, ImmutableMap.of(
                            XADES, XAdES_BASELINE_B,
                            PADES, PAdES_BASELINE_B
                        )
        );

    private static final Map<SignatureParameters.Container, ASiCContainerType> asicContainerTypeMapping =
        ImmutableMap.of(
            ASICS, ASiCContainerType.ASiC_S,
            ASICE, ASiCContainerType.ASiC_E
        );

    private static final Map<SignatureParameters.DigestAlgorithm, DigestAlgorithm> digestAlgorithMapping =
        ImmutableMap.of(
            SHA256, DigestAlgorithm.SHA256,
            SHA384, DigestAlgorithm.SHA384,
            SHA512, DigestAlgorithm.SHA512
        );

    private static final Map<SignatureParameters.Packaging, SignaturePackaging> signaturePackagingMapping =
        ImmutableMap.of(
            ENVELOPED, SignaturePackaging.ENVELOPED,
            ENVELOPING, SignaturePackaging.ENVELOPING
        );

    private static final Map<SignatureParameters.CanonicalizationMethod, String> canonicalizationMethodMapping =
        ImmutableMap.of(
            INCLUSIVE, CanonicalizationMethod.INCLUSIVE
        );

    public static SignatureLevel map(SignatureParameters.Level level, SignatureParameters.Format format) { return signatureLevelMapping.get(level).get(format); }
    public static ASiCContainerType map(SignatureParameters.Container container) { return asicContainerTypeMapping.get(container); }
    public static DigestAlgorithm map(SignatureParameters.DigestAlgorithm digestAlgorithm) { return digestAlgorithMapping.get(digestAlgorithm); }
    public static SignaturePackaging map(SignatureParameters.Packaging packaging) { return signaturePackagingMapping.get(packaging); }
    public static String map(SignatureParameters.CanonicalizationMethod canonicalizationMethod) { return canonicalizationMethodMapping.get(canonicalizationMethod); }


    public static XAdESSignatureParameters mapXAdESParameters(SignatureParameters sp) {
        XAdESSignatureParameters parameters;

        if (sp.getContainer() != null) {
            parameters = new ASiCWithXAdESSignatureParameters();
            ((ASiCWithXAdESSignatureParameters) parameters).aSiC().setContainerType(map(sp.getContainer()));
        } else {
            parameters = new XAdESSignatureParameters();
        }

        parameters.setSignatureLevel(map(sp.getLevel(), XADES));
        parameters.setSignaturePackaging(map(sp.getPackaging()));
        parameters.setDigestAlgorithm(map(sp.getDigestAlgorithm()));
        parameters.setSigningCertificateDigestMethod(map(sp.getDigestAlgorithm()));
        parameters.setEn319132(sp.isEn319132());
        if (sp.getInfoCanonicalization() != null)
            parameters.setSignedInfoCanonicalizationMethod(map(sp.getInfoCanonicalization()));
        if (sp.getPropertiesCanonicalization() != null)
            parameters.setSignedPropertiesCanonicalizationMethod(map(sp.getPropertiesCanonicalization()));

        return parameters;
    }

    public static PAdESSignatureParameters mapPAdESParameters(SignatureParameters sp) {
        var parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(map(sp.getLevel(), PADES));
        parameters.setDigestAlgorithm(map(sp.getDigestAlgorithm()));

        return parameters;
    }
}
