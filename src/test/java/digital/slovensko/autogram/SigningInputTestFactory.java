package digital.slovensko.autogram;

import java.io.File;

import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.SigningInput;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.EMPTY_DOCUMENT;

final class SigningInputTestFactory {
    private SigningInputTestFactory() {
    }

        static EFormAttributes eForm(String identifier, String transformation, String schema, String containerXmlns,
                        String xsdIdentifier, digital.slovensko.autogram.core.eforms.dto.XsltParams xsltParams,
                        boolean embedUsedSchemas) {
                return new EFormAttributes(identifier, transformation, schema, containerXmlns, xsdIdentifier, xsltParams,
                                embedUsedSchemas, null, false, null, null);
        }

    static AutogramDocument document(DSSDocument document) {
        return AutogramDocument.build(document, null);
    }

    static SigningInput forPdf(DSSDocument document, boolean checkPDFACompliance,
            boolean en319132, TSPSource tspSource) {
        var level = tspSource == null ? SignatureLevel.PAdES_BASELINE_B : SignatureLevel.PAdES_BASELINE_T;
        var parameters = parameters(level, DigestAlgorithm.SHA256, null, null, en319132,
                null, null, null, checkPDFACompliance, 640);
        return SigningInput.fromPDFFile(document, parameters);
    }

    static SigningInput fromFile(File file, boolean checkPDFACompliance, SignatureLevel level,
            boolean en319132, boolean plainXmlEnabled) {
        var parameters = parameters(level, DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING, en319132, null, null, null, checkPDFACompliance, 640);
        var document = new FileDocument(file);
        var attributes = attributes(null, true, EFormUtils.getFsFormIdFromFilename(document.getName()),
                parameters.getPropertiesCanonicalization(), parameters.getDigestAlgorithm());
        return SigningInput.fromFile(AutogramDocument.build(document, attributes), parameters, plainXmlEnabled);
    }

    static SigningInput forAsicXades(DSSDocument document, boolean checkPDFACompliance,
            boolean en319132, TSPSource tspSource, boolean plainXmlEnabled) {
        var level = tspSource == null ? SignatureLevel.XAdES_BASELINE_B : SignatureLevel.XAdES_BASELINE_T;
        return prepare(level, DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                en319132, null, null, null, null, true,
                EFormUtils.getFsFormIdFromFilename(document.getName()), checkPDFACompliance, 640,
                document, tspSource, plainXmlEnabled);
    }

    static SigningInput forAsicXades(AutogramDocument document, boolean checkPDFACompliance,
            boolean en319132, TSPSource tspSource, boolean plainXmlEnabled) {
        return forAsicXades(document.toDssDocument(), checkPDFACompliance, en319132, tspSource, plainXmlEnabled);
    }

    static SigningInput forAsicCades(DSSDocument document, boolean checkPDFACompliance,
            boolean en319132, TSPSource tspSource, boolean plainXmlEnabled) {
        var level = tspSource == null ? SignatureLevel.CAdES_BASELINE_B : SignatureLevel.CAdES_BASELINE_T;
        return prepare(level, DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                en319132, null, null, null, null, true,
                EFormUtils.getFsFormIdFromFilename(document.getName()), checkPDFACompliance, 640,
                document, tspSource, plainXmlEnabled);
    }

    static SigningInput prepare(SignatureLevel level, DigestAlgorithm digestAlgorithm,
            ASiCContainerType container, SignaturePackaging packaging, boolean en319132,
            String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            EFormAttributes eFormAttributes, boolean autoLoadEform, String fsFormId,
            boolean checkPDFACompliance, int visualizationWidth, DSSDocument document, TSPSource tspSource,
            boolean plainXmlEnabled) {
        if (document == null)
            throw new SigningParametersException(EMPTY_DOCUMENT);

        var resolvedDigestAlgorithm = digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
        var parameters = parameters(level, resolvedDigestAlgorithm, container, packaging, en319132,
                infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization,
                checkPDFACompliance, visualizationWidth);
        var attributes = attributes(eFormAttributes,
            autoLoadEform || document.getMimeType() != null
                && digital.slovensko.autogram.core.AutogramMimeType.isAsice(document.getMimeType()),
            fsFormId,
                parameters.getPropertiesCanonicalization(), resolvedDigestAlgorithm);
        var autogramDocument = AutogramDocument.build(document, attributes);
        if (!plainXmlEnabled
                && (digital.slovensko.autogram.core.AutogramMimeType.isXML(document.getMimeType())
                    || digital.slovensko.autogram.core.AutogramMimeType.isXDC(document.getMimeType()))
                && autogramDocument.getEFormAttributes().transformation() == null)
            throw new UnknownEformException();

        return SigningInput.fromDocument(autogramDocument, parameters);
    }

    private static SigningParameters parameters(SignatureLevel level, DigestAlgorithm digestAlgorithm,
            ASiCContainerType container, SignaturePackaging packaging, boolean en319132,
            String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int visualizationWidth) {
        return SigningParameters.buildParameters(level, digestAlgorithm, container, packaging, en319132,
                infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization,
                checkPDFACompliance, visualizationWidth);
    }

    private static EFormAttributes attributes(EFormAttributes attributes, boolean autoLoadEform, String fsFormId,
            String propertiesCanonicalization, DigestAlgorithm digestAlgorithm) {
        if (attributes == null)
            return new EFormAttributes(null, null, null, null, null, null, false, fsFormId,
                    autoLoadEform, propertiesCanonicalization, digestAlgorithm);

        return new EFormAttributes(attributes.identifier(), attributes.transformation(), attributes.schema(),
                attributes.containerXmlns(), attributes.xsdIdentifier(), attributes.xsltParams(),
                attributes.embedUsedSchemas(), fsFormId != null ? fsFormId : attributes.fsFormId(),
                autoLoadEform || attributes.autoLoadEform(), propertiesCanonicalization, digestAlgorithm);
    }
}
