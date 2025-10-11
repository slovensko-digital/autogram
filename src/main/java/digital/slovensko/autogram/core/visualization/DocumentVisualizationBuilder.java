package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import static digital.slovensko.autogram.core.AutogramMimeType.isAsice;
import static digital.slovensko.autogram.core.AutogramMimeType.isImage;
import static digital.slovensko.autogram.core.AutogramMimeType.isPDF;
import static digital.slovensko.autogram.core.AutogramMimeType.isTxt;
import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;

public class DocumentVisualizationBuilder {

    private final DSSDocument document;
    private final SigningParameters parameters;
    private final Executor executor;

    private DocumentVisualizationBuilder(DSSDocument document, SigningParameters parameters, Executor executor) {
        this.document = document;
        this.parameters = parameters;
        this.executor = executor;
    }

    public static Visualization fromJob(SigningJob job, UserSettings userSettings, Executor executor) throws IOException, ParserConfigurationException, SAXException {
        return new DocumentVisualizationBuilder(job.getDocument(), job.getParameters(), executor).build(job, userSettings);
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

            if (transformationOutputMimeType.equals("XHTML"))
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
            return new PDFVisualization(documentToDisplay, job, userSettings, executor);

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
