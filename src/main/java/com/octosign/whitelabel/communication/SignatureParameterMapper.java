package com.octosign.whitelabel.communication;

import com.google.common.collect.ImmutableMap;
import com.octosign.whitelabel.ui.Main;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pdf.pdfbox.visible.PdfBoxNativeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.awt.*;
import java.util.Map;
import java.util.Objects;

import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.EXCLUSIVE;
import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.INCLUSIVE;
import static com.octosign.whitelabel.communication.SignatureParameters.Container.ASICE;
import static com.octosign.whitelabel.communication.SignatureParameters.Container.ASICS;
import static com.octosign.whitelabel.communication.SignatureParameters.DigestAlgorithm.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Level.*;
import static com.octosign.whitelabel.communication.SignatureParameters.Packaging.*;

public class SignatureParameterMapper {
    private static final Map<SignatureParameters.Level, SignatureLevel> signatureLevelMapping =
        Map.of(
            XADES_BASELINE_B, SignatureLevel.XAdES_BASELINE_B,
            XADES_BASELINE_T, SignatureLevel.XAdES_BASELINE_T,
            XADES_BASELINE_LT, SignatureLevel.XAdES_BASELINE_LT,
            XADES_BASELINE_LTA, SignatureLevel.XAdES_BASELINE_LTA,

            PADES_BASELINE_B, SignatureLevel.PAdES_BASELINE_B,
            PADES_BASELINE_T, SignatureLevel.PAdES_BASELINE_T,
            PADES_BASELINE_LT, SignatureLevel.PAdES_BASELINE_LT,
            PADES_BASELINE_LTA, SignatureLevel.PAdES_BASELINE_LTA
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
            ENVELOPING, SignaturePackaging.ENVELOPING,
            DETACHED, SignaturePackaging.DETACHED,
            INTERNALLY_DETACHED, SignaturePackaging.INTERNALLY_DETACHED
        );

    private static final Map<SignatureParameters.CanonicalizationMethod, String> canonicalizationMethodMapping =
        ImmutableMap.of(
            EXCLUSIVE, CanonicalizationMethod.EXCLUSIVE,
            INCLUSIVE, CanonicalizationMethod.INCLUSIVE
        );

    public static SignatureLevel map(SignatureParameters.Level level) {
        return signatureLevelMapping.get(level);
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

    public static AbstractSignatureParameters<?> map(SignatureParameters source) {
        return switch (source.getFormat()) {
            case XADES: yield buildXAdESParameters(source);
            case PADES: yield buildPAdESParameters(source);

            default: throw new AssertionError();
        };
    }

    private static ASiCWithXAdESSignatureParameters buildXAdESParameters(SignatureParameters sp) {
        var parameters = new ASiCWithXAdESSignatureParameters();

        parameters.aSiC().setContainerType(map(sp.getContainer()));
        parameters.aSiC().setMimeType(sp.getFileMimeType());

        parameters.setSignatureLevel(map(sp.getLevel()));
        parameters.setSignaturePackaging(map(sp.getPackaging()));
        parameters.setDigestAlgorithm(map(sp.getDigestAlgorithm()));
        parameters.setSigningCertificateDigestMethod(map(sp.getDigestAlgorithm()));
        parameters.setSignedInfoCanonicalizationMethod(map(sp.getInfoCanonicalization()));
        parameters.setSignedPropertiesCanonicalizationMethod(map(sp.getPropertiesCanonicalization()));

        parameters.setEn319132(sp.isEn319132());

        return parameters;
    }

    private static PAdESSignatureParameters buildPAdESParameters(SignatureParameters sp) {
        var parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(map(sp.getLevel()));
        parameters.setDigestAlgorithm(map(sp.getDigestAlgorithm()));
        parameters.setSignaturePackaging(map(sp.getPackaging()));

        return parameters;
    }

    // not desired (yet)
    private static SignatureImageParameters configureVisibleSignature() {
        SignatureImageParameters signatureImageParameters = new SignatureImageParameters();
        signatureImageParameters.setImage(new InMemoryDocument(Objects.requireNonNull(Main.class.getResourceAsStream("icon.png"))));

        SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
        signatureImageParameters.setFieldParameters(fieldParameters);
        // the origin is the left and top corner of the page
        fieldParameters.setOriginX(200);
        fieldParameters.setOriginY(400);
        fieldParameters.setWidth(40);
        fieldParameters.setHeight(40);

        // Visible signature positioning

        // Allows alignment of a signature field horizontally to a page. Allows the following values:
        /* _NONE_ (_DEFAULT value._ None alignment is applied, coordinates are counted from the left page side);
           _LEFT_ (the signature is aligned to the left side, coordinated are counted from the left page side);
           _CENTER_ (the signature is aligned to the center of the page, coordinates are counted automatically);
           _RIGHT_ (the signature is aligned to the right side, coordinated are counted from the right page side). */
        signatureImageParameters.setAlignmentHorizontal(VisualSignatureAlignmentHorizontal.CENTER);

        // Allows alignment of a signature field vertically to a page. Allows the following values:
        /* _NONE_ (_DEFAULT value._ None alignment is applied, coordinated are counted from the top side of a page);
           _TOP_ (the signature is aligned to a top side, coordinated are counted from the top page side);
           _MIDDLE_ (the signature aligned to a middle of a page, coordinated are counted automatically);
           _BOTTOM_ (the signature is aligned to a bottom side, coordinated are counted from the bottom page side). */
        signatureImageParameters.setAlignmentVertical(VisualSignatureAlignmentVertical.TOP);

        // Rotates the signature field and changes the coordinates' origin respectively to its values as following:
        /* _NONE_ (_DEFAULT value._ No rotation is applied. The origin of coordinates begins from the top left corner of a page);
           _AUTOMATIC_ (Rotates a signature field respectively to the page's rotation. Rotates the signature field on the same value as a defined in a PDF page);
           _ROTATE_90_ (Rotates a signature field for a 90&#176; clockwise. Coordinates' origin begins from top right page corner);
           _ROTATE_180_ (Rotates a signature field for a 180&#176; clockwise. Coordinates' origin begins from the bottom right page corner);
           _ROTATE_270_ (Rotates a signature field for a 270&#176; clockwise. Coordinates' origin begins from the bottom left page corner). */
        signatureImageParameters.setRotation(VisualSignatureRotation.AUTOMATIC);

        // Defines a zoom of the image. The value is applied to width and height of a signature field.
        // The value must be defined in percentage (default value is 100, no zoom is applied).
        signatureImageParameters.setZoom(50);

        // Specifies a background color for a signature field.
        signatureImageParameters.setBackgroundColor(Color.GREEN);

        // Visible signature dimensions

        // Allows alignment of a signature field horizontally to a page. Allows the following values:
        /* _NONE_ (_DEFAULT value._ None alignment is applied, coordinates are counted from the left page side);
           _LEFT_ (the signature is aligned to the left side, coordinated are counted from the left page side);
           _CENTER_ (the signature is aligned to the center of the page, coordinates are counted automatically);
           _RIGHT_ (the signature is aligned to the right side, coordinated are counted from the right page side). */
        signatureImageParameters.setAlignmentHorizontal(VisualSignatureAlignmentHorizontal.CENTER);

        // Allows alignment of a signature field vertically to a page. Allows the following values:
        /* _NONE_ (_DEFAULT value._ None alignment is applied, coordinated are counted from the top side of a page);
           _TOP_ (the signature is aligned to a top side, coordinated are counted from the top page side);
           _MIDDLE_ (the signature aligned to a middle of a page, coordinated are counted automatically);
           _BOTTOM_ (the signature is aligned to a bottom side, coordinated are counted from the bottom page side). */
        signatureImageParameters.setAlignmentVertical(VisualSignatureAlignmentVertical.TOP);

        // Rotates the signature field and changes the coordinates' origin respectively to its values as following:
        /* _NONE_ (_DEFAULT value._ No rotation is applied. The origin of coordinates begins from the top left corner of a page);
           _AUTOMATIC_ (Rotates a signature field respectively to the page's rotation. Rotates the signature field on the same value as a defined in a PDF page);
           _ROTATE_90_ (Rotates a signature field for a 90&#176; clockwise. Coordinates' origin begins from top right page corner);
           _ROTATE_180_ (Rotates a signature field for a 180&#176; clockwise. Coordinates' origin begins from the bottom right page corner);
           _ROTATE_270_ (Rotates a signature field for a 270&#176; clockwise. Coordinates' origin begins from the bottom left page corner). */
        signatureImageParameters.setRotation(VisualSignatureRotation.AUTOMATIC);

        // Defines a zoom of the image. The value is applied to width and height of a signature field.
        // The value must be defined in percentage (default value is 100, no zoom is applied).
        signatureImageParameters.setZoom(50);

        // Specifies a background color for a signature field.
        signatureImageParameters.setBackgroundColor(Color.GREEN);

        // Text Parameters

        // Instantiates a SignatureImageTextParameters object
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        // Allows you to set a DSSFont object that defines the text style (see more information in the section "Fonts usage")
        textParameters.setFont(new PdfBoxNativeFont(PDType1Font.HELVETICA));
        // Defines the text content
        textParameters.setText("My visual signature \n #1");
        // Defines the color of the characters
        textParameters.setTextColor(Color.BLUE);
        // Defines the background color for the area filled out by the text
        textParameters.setBackgroundColor(Color.YELLOW);
        // Defines a padding between the text and a border of its bounding area
        textParameters.setPadding(20);

        // Text and image combination

        // Specifies a text position relatively to an image (Note: applicable only for joint image+text visible signatures).
        // Thus with _SignerPosition.LEFT_ value, the text will be placed on the left side,
        // and image will be aligned to the right side inside the signature field
        textParameters.setSignerTextPosition(SignerTextPosition.LEFT);
        // Specifies a horizontal alignment of a text with respect to its area
        textParameters.setSignerTextHorizontalAlignment(SignerTextHorizontalAlignment.RIGHT);
        // Specifies a vertical alignment of a text block with respect to a signature field area
        textParameters.setSignerTextVerticalAlignment(SignerTextVerticalAlignment.TOP);

        return signatureImageParameters;
    }
}
