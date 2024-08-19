package digital.slovensko.autogram.core.visualization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.model.ProtectedDSSDocument;
import eu.europa.esig.dss.model.DSSDocument;

import org.xml.sax.SAXException;

import digital.slovensko.autogram.core.AutogramMimeType;
import static digital.slovensko.autogram.core.AutogramMimeType.*;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class DocumentVisualizationBuilder {

    private final ProtectedDSSDocument document;
    private final SigningParameters parameters;

    private DocumentVisualizationBuilder(ProtectedDSSDocument document, SigningParameters parameters) {
        this.document = document;
        this.parameters = parameters;
    }

    public static Visualization fromJob(SigningJob job, UserSettings userSettings) throws IOException, ParserConfigurationException, SAXException {
        return new DocumentVisualizationBuilder(job.getDocument(), job.getParameters()).build(job, userSettings);
    }

    private Visualization build(SigningJob job, UserSettings userSettings) throws IOException, ParserConfigurationException, SAXException {
        return createVisualization(job, userSettings);
    }

    private Visualization createVisualization(SigningJob job, UserSettings userSettings)
        throws IOException, ParserConfigurationException, SAXException {

        var documentToDisplay = document;
        if (isAsice(documentToDisplay.getMimeType())) {
            try {
                documentToDisplay = AsicContainerUtils.getOriginalDocument(document);
            } catch (AutogramException e) {
                return new UnsupportedVisualization(job);
            }
        }

        var transformation = parameters.getTransformation();

        if (isDocumentSupportingTransformation(documentToDisplay) && isTranformationAvailable(transformation)) {
            var transformationOutputMimeType = parameters.getXsltDestinationType();

            if (transformationOutputMimeType.equals("HTML"))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            if (transformationOutputMimeType.equals("TXT"))
                return new PlainTextVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            return new UnsupportedVisualization(job);
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

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
}
