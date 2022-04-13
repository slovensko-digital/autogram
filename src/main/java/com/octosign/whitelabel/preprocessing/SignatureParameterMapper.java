package com.octosign.whitelabel.preprocessing;

import com.google.common.collect.ImmutableMap;
import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignatureParameters;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.util.Map;

import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Container.*;
import static com.octosign.whitelabel.communication.SignatureParameters.DigestAlgorithm.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Format.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Level.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Packaging.*;
import static com.octosign.whitelabel.preprocessing.XDCTransformer.*;
import static eu.europa.esig.dss.enumerations.SignatureLevel.*;

public class SignatureParameterMapper {
    private static final Map<SignatureParameters.Level, Map<SignatureParameters.Format, SignatureLevel>> signatureLevelMapping =
        ImmutableMap.of(
            BASELINE_B, ImmutableMap.of(
                            XADES, XAdES_BASELINE_B,
                            PADES, PAdES_BASELINE_B,
                            CADES, CAdES_BASELINE_B
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

    private static final Map<MimeType, XDCTransformer.DestinationMediaType> mimeTypeToDestinationMediaTypeMapping =
            ImmutableMap.of(
                    MimeType.PLAIN, DestinationMediaType.TXT,
                    MimeType.HTML, DestinationMediaType.HTML
            );

    public static SignatureLevel map(SignatureParameters.Level level, SignatureParameters.Format format) {
        return signatureLevelMapping.get(level).get(format);
    }

    public static ASiCContainerType map(SignatureParameters.Container container) {
        return asicContainerTypeMapping.get(container);
    }

    public static DigestAlgorithm map(SignatureParameters.DigestAlgorithm digestAlgorithm) {
        return digestAlgorithMapping.get(digestAlgorithm);
    }

    public static SignaturePackaging map(SignatureParameters.Packaging packaging) {
        return signaturePackagingMapping.get(packaging);
    }

    public static String map(SignatureParameters.CanonicalizationMethod canonicalizationMethod) {
        return canonicalizationMethodMapping.get(canonicalizationMethod);
    }

    public static DestinationMediaType map(MimeType transformationOutputMimeType) {
        return mimeTypeToDestinationMediaTypeMapping.get(transformationOutputMimeType);
    }

    /*
        Parameter mapping for XADES signatures without XDC or ASICE
     */
    public static XAdESSignatureParameters mapXAdESParameters(SignatureParameters sp) {
        var parameters = new XAdESSignatureParameters();

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

    /*
        Parameter mapping for XADES signatures within XDC or ASICE in general
     */
    public static ASiCWithXAdESSignatureParameters mapASiCWithXAdESParameters(SignatureParameters sp) {
        var parameters = new ASiCWithXAdESSignatureParameters();

        parameters.aSiC().setContainerType(map(sp.getContainer()));
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

    /*
        Parameter mapping for CADES signatures
    */
    public static ASiCWithCAdESSignatureParameters mapASiCWithCAdESParameters(SignatureParameters sp) {
        var parameters = new ASiCWithCAdESSignatureParameters();

        parameters.aSiC().setContainerType(map(sp.getContainer()));
        parameters.setSignatureLevel(map(sp.getLevel(), CADES));
        parameters.setSignaturePackaging(map(sp.getPackaging()));
        parameters.setDigestAlgorithm(map(sp.getDigestAlgorithm()));
        return parameters;
    }


    /*
        Parameter mapping for PADES signatures
    */
    public static PAdESSignatureParameters mapPAdESParameters(SignatureParameters sp) {
        var parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(map(sp.getLevel(), PADES));
        parameters.setDigestAlgorithm(map(sp.getDigestAlgorithm()));

        return parameters;
    }
}
