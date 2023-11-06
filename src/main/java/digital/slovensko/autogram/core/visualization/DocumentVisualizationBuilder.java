package digital.slovensko.autogram.core.visualization;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

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

    private final DSSDocument document;
    private final SigningParameters parameters;

    private DocumentVisualizationBuilder(DSSDocument document, SigningParameters parameters) {
        this.document = document;
        this.parameters = parameters;
    }

    public static Visualization fromJob(SigningJob job) throws IOException, ParserConfigurationException, SAXException {
        return new DocumentVisualizationBuilder(job.getDocument(), job.getParameters()).build(job);
    }

    private Visualization build(SigningJob job) throws IOException, ParserConfigurationException, SAXException {
        return createVisualization(job);
    }

    private Visualization createVisualization(SigningJob job)
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
        var transformationOutputMimeType = parameters.getTransformationOutputMimeTypeString();

        if (isDocumentSupportingTransformation(documentToDisplay) && isTranformationAvailable(transformation)) {
            if (transformationOutputMimeType.equals("HTML"))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            if (transformationOutputMimeType.equals("TXT"))
                return new PlainTextVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

            return new UnsupportedVisualization(job);
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), job);

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.TEXT))
            return new PlainTextVisualization(new String(documentToDisplay.openStream().readAllBytes()), job);

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.PDF))
            return new PDFVisualization(documentToDisplay, job);

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.JPEG) || documentToDisplay.getMimeType().equals(MimeTypeEnum.PNG))
            return new ImageVisualization(documentToDisplay, job);

        return new UnsupportedVisualization(job);
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    private boolean isDocumentSupportingTransformation(DSSDocument document) {
        return document.getMimeType().equals(AutogramMimeType.XML_DATACONTAINER)
            || document.getMimeType().equals(AutogramMimeType.APPLICATION_XML)
            || document.getMimeType().equals(MimeTypeEnum.XML);
    }
}
