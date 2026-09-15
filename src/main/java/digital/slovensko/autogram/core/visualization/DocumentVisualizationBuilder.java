package digital.slovensko.autogram.core.visualization;

import static digital.slovensko.autogram.core.dto.AutogramMimeType.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import digital.slovensko.autogram.core.FailedVisualizationException;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.eforms.EFormUtils;

import org.xml.sax.SAXException;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class DocumentVisualizationBuilder {
    private final AutogramDocument document;

    private DocumentVisualizationBuilder(AutogramDocument document) {
        this.document = document;
    }

    public static Visualization fromDocument(AutogramDocument document) throws FailedVisualizationException {
        try {
            return new DocumentVisualizationBuilder(document).build();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new FailedVisualizationException(e);
        }
    }

    private Visualization build() throws IOException, ParserConfigurationException, SAXException {
        var resolvedTransformation = resolveTransformation();
        var transformation = resolvedTransformation.transformation();
        var transformationOutputMimeType = resolvedTransformation.outputType();

        var documentToDisplay = document.toDssDocument();

        if (isDocumentSupportingTransformation() && isTranformationAvailable(transformation)) {
            if ("HTML".equals(transformationOutputMimeType))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), documentToDisplay.getName());

            if ("XHTML".equals(transformationOutputMimeType))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), documentToDisplay.getName());

            if ("TXT".equals(transformationOutputMimeType))
                return new PlainTextVisualization(EFormUtils.transform(documentToDisplay, transformation), documentToDisplay.getName());

            return new UnsupportedVisualization(documentToDisplay.getName());
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(new String(documentToDisplay.openStream().readAllBytes(), StandardCharsets.UTF_8), documentToDisplay.getName());

        if (isTxt(documentToDisplay.getMimeType()))
            return new PlainTextVisualization(new String(documentToDisplay.openStream().readAllBytes(), StandardCharsets.UTF_8), documentToDisplay.getName());

        if (isPDF(documentToDisplay.getMimeType()))
            return new PDFVisualization(documentToDisplay);

        if (isImage(documentToDisplay.getMimeType()))
            return new ImageVisualization(documentToDisplay);

        return new UnsupportedVisualization(documentToDisplay.getName());
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    private boolean isDocumentSupportingTransformation() {
        return document.isXDC() || document.isXML();
    }

    private ResolvedTransformation resolveTransformation() {
        String transformation = null;
        String outputType = null;

        if (document.getEFormAttributes() != null) {
            if (document.getEFormAttributes().transformation() != null)
                transformation = document.getEFormAttributes().transformation();

            if (document.getEFormAttributes().xsltParams() != null)
                outputType = normalizeOutputType(document.getEFormAttributes().xsltParams().destinationType());
        }

        if (transformation != null && outputType == null)
            outputType = normalizeOutputType(EFormUtils.extractTransformationOutputMimeTypeString(transformation));

        return new ResolvedTransformation(transformation, outputType);
    }

    private String normalizeOutputType(String outputType) {
        if (outputType == null)
            return null;

        return outputType.toUpperCase(Locale.ROOT);
    }

    private record ResolvedTransformation(String transformation, String outputType) {
    }
}
