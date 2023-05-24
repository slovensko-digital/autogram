package digital.slovensko.autogram.server.dto;

import static digital.slovensko.autogram.server.dto.ServerSigningParameters.LocalCanonicalizationMethod.*;

import java.util.Arrays;
import java.util.Base64;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.server.errors.UnsupportedSignatureLevelExceptionError;
import eu.europa.esig.dss.enumerations.*;

public class ServerSigningParameters {
    public enum LocalCanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE,
        INCLUSIVE_WITH_COMMENTS,
        EXCLUSIVE_WITH_COMMENTS,
        INCLUSIVE_11,
        INCLUSIVE_11_WITH_COMMENTS
    }

    private final ASiCContainerType container;
    private final SignatureLevel level;
    private final String containerXmlns;
    private final String schema;
    private final String transformation;
    private final SignaturePackaging packaging;
    private final DigestAlgorithm digestAlgorithm;
    private final Boolean en319132;
    private final LocalCanonicalizationMethod infoCanonicalization;
    private final LocalCanonicalizationMethod propertiesCanonicalization;
    private final LocalCanonicalizationMethod keyInfoCanonicalization;
    private final String identifier;
    private final boolean checkPDFACompliance;
    private final int previewWidth;

    public ServerSigningParameters(SignatureLevel level, ASiCContainerType container,
                                   String containerFilename, String containerXmlns, SignaturePackaging packaging,
                                   DigestAlgorithm digestAlgorithm,
                                   Boolean en319132, LocalCanonicalizationMethod infoCanonicalization,
                                   LocalCanonicalizationMethod propertiesCanonicalization, LocalCanonicalizationMethod keyInfoCanonicalization,
                                   String schema, String transformation,
                                   String Identifier, boolean checkPDFACompliance, int previewWidth) {
        this.level = level;
        this.container = container;
        this.containerXmlns = containerXmlns;
        this.packaging = packaging;
        this.digestAlgorithm = digestAlgorithm;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.schema = schema;
        this.transformation = transformation;
        this.identifier = Identifier;
        this.checkPDFACompliance = checkPDFACompliance;
        this.previewWidth = previewWidth;
    }

    public SigningParameters getSigningParameters(boolean isBase64) {
        return new SigningParameters(
                getSignatureLevel(),
                getContainer(),
                containerXmlns,
                packaging,
                digestAlgorithm,
                en319132,
                getCanonicalizationMethodString(infoCanonicalization),
                getCanonicalizationMethodString(propertiesCanonicalization),
                getCanonicalizationMethodString(keyInfoCanonicalization),
                getSchema(isBase64),
                getTransformation(isBase64),
                identifier, checkPDFACompliance, previewWidth);
    }

    private String getTransformation(boolean isBase64) {
        if (transformation == null)
            return null;

        if (isBase64)
            return new String(Base64.getDecoder().decode(transformation));

        return transformation;
    }

    private String getSchema(boolean isBase64) {
        if (schema == null)
            return null;

        if (isBase64)
            return new String(Base64.getDecoder().decode(schema));

        return schema;
    }

    private static String getCanonicalizationMethodString(LocalCanonicalizationMethod method) {
        if (method == INCLUSIVE)
            return CanonicalizationMethod.INCLUSIVE;
        if (method == EXCLUSIVE)
            return CanonicalizationMethod.EXCLUSIVE;
        if (method == INCLUSIVE_WITH_COMMENTS)
            return CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS;
        if (method == EXCLUSIVE_WITH_COMMENTS)
            return CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;
        if (method == INCLUSIVE_11)
            return CanonicalizationMethod.INCLUSIVE_11;
        if (method == INCLUSIVE_11_WITH_COMMENTS)
            return CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS;

        return null;
    }

    private SignatureLevel getSignatureLevel() {
        return level;
    }

    private ASiCContainerType getContainer() {
        return container;
    }

    public void validate(MimeType mimeType) {
        if (level == null)
            throw new RequestValidationException("Parameters.Level is required", "");

        var supportedLevels = Arrays.asList(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B,
                SignatureLevel.CAdES_BASELINE_B);

        if (!supportedLevels.contains(level))
            throw new UnsupportedSignatureLevelExceptionError(level.name());

        if (level.getSignatureForm() == SignatureForm.PAdES) {
            if (!mimeType.equals(MimeTypeEnum.PDF))
                throw new RequestValidationException("PayloadMimeType and Parameters.Level mismatch",
                        "Parameters.Level: PAdES is not supported for this payload: " + mimeType.getMimeTypeString());

            if (container != null)
                throw new RequestValidationException("Parameters.Container is not supported for PAdES",
                        "PAdES signature cannot be in a container");
        }

        if (level.getSignatureForm() == SignatureForm.XAdES) {
            if (!isXMLMimeType(mimeType) && !isXDCMimeType(mimeType) && container == null)
                if (!(packaging != null && packaging == SignaturePackaging.ENVELOPING))
                    throw new RequestValidationException(
                            "PayloadMimeType, Parameters.Level, Parameters.Container and Parameters.Packaging mismatch",
                            "Parameters.Level: XAdES without container and ENVELOPED packaging is not supported for this payload: "
                                    + mimeType.getMimeTypeString());
        }

        if (containerXmlns != null && containerXmlns.contains("xmldatacontainer")
                && !isXDCMimeType(mimeType)) {

            if (transformation == null || transformation.isEmpty())
                throw new RequestValidationException("Parameters.Transformation is null",
                        "Parameters.Transformation is required when creating XML datacontainer - when Parameters.ContainerXmlns is set to xmldatacontainer");

            if (schema == null || schema.isEmpty())
                throw new RequestValidationException("Parameters.Schema is null",
                        "Parameters.Schema is required when creating XML datacontainer - when Parameters.ContainerXmlns is set to xmldatacontainer");

            if (identifier == null || identifier.isEmpty())
                throw new RequestValidationException("Parameters.Identifier is null",
                        "Parameters.Identifier is required when creating XML datacontainer - when Parameters.ContainerXmlns is set to xmldatacontainer");

            if (!isXMLMimeType(mimeType))
                throw new RequestValidationException("PayloadMimeType and Parameters.ContainerXmlns mismatch",
                        "Parameters.ContainerXmlns: XML datacontainer is not supported for this payload: "
                                + mimeType.getMimeTypeString());
        }
    }

    private static boolean isXMLMimeType(MimeType mimeType) {
        return mimeType.equals(MimeTypeEnum.XML) || mimeType.equals(AutogramMimeType.APPLICATION_XML);
    }

    private static boolean isXDCMimeType(MimeType mimeType) {
        return mimeType.equals(AutogramMimeType.XML_DATACONTAINER);
    }
}
