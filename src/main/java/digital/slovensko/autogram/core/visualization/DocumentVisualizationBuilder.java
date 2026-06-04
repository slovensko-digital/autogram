package digital.slovensko.autogram.core.visualization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.eforms.EFormResourcesBuilder;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.model.DSSDocument;

import org.xml.sax.SAXException;

import static digital.slovensko.autogram.core.AutogramMimeType.*;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class DocumentVisualizationBuilder {

    private final DSSDocument document;
    private final SigningParameters parameters;

    private DocumentVisualizationBuilder(DSSDocument document, SigningParameters parameters) {
        this.document = document;
        this.parameters = parameters;
    }

    public static Visualization fromJob(SigningJob job, UserSettings userSettings) throws IOException, ParserConfigurationException, SAXException {
        return fromDocument(job, job.getDocument(), userSettings);
    }

    public static Visualization fromDocument(SigningJob job, DSSDocument document, UserSettings userSettings)
            throws IOException, ParserConfigurationException, SAXException {
        return new DocumentVisualizationBuilder(document, job.getParameters()).build(job, userSettings);
    }

    private Visualization build(SigningJob job, UserSettings userSettings) throws IOException, ParserConfigurationException, SAXException {
        return createVisualization(job, userSettings);
    }

    private Visualization createVisualization(SigningJob job, UserSettings userSettings)
        throws IOException, ParserConfigurationException, SAXException {

        var documentToDisplay = document;
        if (isAsice(documentToDisplay.getMimeType())) {
            try {
                documentToDisplay = AsicContainerUtils.getOriginalDocuments(document).get(0);
            } catch (AutogramException e) {
                return new UnsupportedVisualization(job);
            }
        }

        var resolvedTransformation = resolveTransformation(documentToDisplay);
        var transformation = resolvedTransformation.transformation();
        var transformationOutputMimeType = resolvedTransformation.outputType();

        if (isDocumentSupportingTransformation(documentToDisplay) && isTranformationAvailable(transformation)) {
            if ("HTML".equals(transformationOutputMimeType))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            if ("XHTML".equals(transformationOutputMimeType))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            if ("TXT".equals(transformationOutputMimeType))
                return new PlainTextVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            return new UnsupportedVisualization(job);
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(new String(documentToDisplay.openStream().readAllBytes(), StandardCharsets.UTF_8),
                    job);

        if (isTxt(documentToDisplay.getMimeType()))
            return new PlainTextVisualization(new String(documentToDisplay.openStream().readAllBytes(), StandardCharsets.UTF_8), job);

        if (isPDF(documentToDisplay.getMimeType()))
            return new PDFVisualization(documentToDisplay, job, userSettings);

        if (isImage(documentToDisplay.getMimeType()))
            return new ImageVisualization(documentToDisplay, job);

        return new UnsupportedVisualization(job);
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    private boolean isDocumentSupportingTransformation(DSSDocument document) {
        return isXDC(document.getMimeType()) || isXML(document.getMimeType());
    }

    private ResolvedTransformation resolveTransformation(DSSDocument documentToDisplay) {
        var transformation = parameters.getTransformation();
        var outputType = normalizeOutputType(parameters.getXsltDestinationType());

        var eFormAttributes = resolveDocumentEFormAttributes(documentToDisplay);
        if (eFormAttributes != null) {
            if (eFormAttributes.transformation() != null)
                transformation = eFormAttributes.transformation();

            if (eFormAttributes.xsltParams() != null)
                outputType = normalizeOutputType(eFormAttributes.xsltParams().destinationType());
        }

        if (transformation != null && outputType == null)
            outputType = normalizeOutputType(EFormUtils.extractTransformationOutputMimeTypeString(transformation));

        return new ResolvedTransformation(transformation, outputType);
    }

    private EFormAttributes resolveDocumentEFormAttributes(DSSDocument documentToDisplay) {
        if (!isDocumentSupportingTransformation(documentToDisplay))
            return null;

        try {
            var eFormResources = EFormResourcesBuilder.build(documentToDisplay,
                    EFormUtils.getFsFormIdFromFilename(documentToDisplay.getName()), null, null,
                    parameters.getPropertiesCanonicalization());
            if (eFormResources == null)
                return null;

            return eFormResources.getEformAttributes();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeOutputType(String outputType) {
        if (outputType == null)
            return null;

        return outputType.toUpperCase(Locale.ROOT);
    }

    private record ResolvedTransformation(String transformation, String outputType) {
    }
}
