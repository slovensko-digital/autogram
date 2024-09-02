package digital.slovensko.autogram.core.visualization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.model.AutogramDocument;
import eu.europa.esig.dss.model.DSSDocument;

import org.xml.sax.SAXException;

import static digital.slovensko.autogram.core.AutogramMimeType.*;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class DocumentVisualizationBuilder {

    private final AutogramDocument document;
    private final SigningParameters parameters;

    private DocumentVisualizationBuilder(AutogramDocument document, SigningParameters parameters) {
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
        if (isAsice(documentToDisplay.getDocument().getMimeType())) {
            try {
                documentToDisplay = new AutogramDocument(AsicContainerUtils.getOriginalDocument(document.getDocument()));
            } catch (AutogramException e) {
                return new UnsupportedVisualization(job);
            }
        }

        var transformation = parameters.getTransformation();

        if (isDocumentSupportingTransformation(documentToDisplay.getDocument()) && isTranformationAvailable(transformation)) {
            var transformationOutputMimeType = parameters.getXsltDestinationType();

            if (transformationOutputMimeType.equals("HTML"))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay.getDocument(), transformation), job);

            if (transformationOutputMimeType.equals("TXT"))
                return new PlainTextVisualization(EFormUtils.transform(documentToDisplay.getDocument(), transformation), job);

            return new UnsupportedVisualization(job);
        }

        if (documentToDisplay.getDocument().getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(EFormUtils.transform(documentToDisplay.getDocument(), transformation), job);

        if (isTxt(documentToDisplay.getDocument().getMimeType()))
            return new PlainTextVisualization(new String(documentToDisplay.getDocument().openStream().readAllBytes(), StandardCharsets.UTF_8), job);

        if (isPDF(documentToDisplay.getDocument().getMimeType()))
            return new PDFVisualization(documentToDisplay, job, userSettings);

        if (isImage(documentToDisplay.getDocument().getMimeType()))
            return new ImageVisualization(documentToDisplay.getDocument(), job);

        return new UnsupportedVisualization(job);
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    private boolean isDocumentSupportingTransformation(DSSDocument document) {
        return isXDC(document.getMimeType()) || isXML(document.getMimeType());
    }
}
