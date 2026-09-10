package digital.slovensko.autogram.core;

import java.io.File;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.TEXT_WITH_CHARSET;
import static digital.slovensko.autogram.core.AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET;
import static digital.slovensko.autogram.core.AutogramMimeType.isTxt;
import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.WRONG_MIME_TYPE;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.XSLT_NO_XDC;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_MIME_TYPE;

public class AutogramDocument {
    private final DSSDocument dssDocument;
    private final EFormAttributes eFormAttributes;

    private AutogramDocument(DSSDocument dssDocument) {
        this(dssDocument, null);
    }

    private AutogramDocument(DSSDocument dssDocument, EFormAttributes eFormAttributes) {
        this.dssDocument = normalize(dssDocument);
        this.eFormAttributes = eFormAttributes;
    }

    public static AutogramDocument fromDssDocument(DSSDocument dssDocument) {
        return new AutogramDocument(dssDocument);
    }

    public static AutogramDocument fromFile(File file) {
        return new AutogramDocument(new FileDocument(file));
    }

    public static AutogramDocument fromContent(byte[] content, String filename, MimeType mimeType) {
        return new AutogramDocument(new InMemoryDocument(content, filename, mimeType));
    }

    public AutogramDocument withEFormAttributes(EFormAttributes eFormAttributes) {
        return new AutogramDocument(dssDocument, eFormAttributes);
    }

    public DSSDocument toDssDocument() {
        return dssDocument;
    }

    public MimeType getMimeType() {
        return dssDocument.getMimeType();
    }

    public String getName() {
        return dssDocument.getName();
    }

    public EFormAttributes getEFormAttributes() {
        return eFormAttributes;
    }

    public PreparedEFormAttributes prepareEFormAttributes(EFormAttributes attributes, boolean autoLoadEform,
            String fsFormId, String propertiesCanonicalization, DigestAlgorithm digestAlgorithm,
            boolean plainXmlEnabled) throws AutogramException {
        if (getMimeType() == null)
            throw new SigningParametersException(NO_MIME_TYPE);

        var extractedDocument = dssDocument;
        var isAsiceDocument = AutogramMimeType.isAsice(getMimeType());
        if (isAsiceDocument)
            extractedDocument = normalize(AsicContainerUtils.getOriginalDocuments(dssDocument).get(0));

        var translatedFsFormId = EFormUtils.translateFsFormId(fsFormId);
        var preparedAttributes = EFormAttributes.build(attributes, autoLoadEform || isAsiceDocument,
            translatedFsFormId, extractedDocument, propertiesCanonicalization);
        var extractedMimeType = extractedDocument.getMimeType();

        if (preparedAttributes.containerXmlns() != null
                && preparedAttributes.containerXmlns().contains("xmldatacontainer")
                && !isXML(extractedMimeType) && !isXDC(extractedMimeType))
            throw new SigningParametersException(WRONG_MIME_TYPE);

        if (isXDC(extractedMimeType) || isXML(extractedMimeType))
            XDCValidator.validateXml(preparedAttributes.schema(), preparedAttributes.transformation(), extractedDocument,
                propertiesCanonicalization, digestAlgorithm, preparedAttributes.embedUsedSchemas());

        if (!isXDC(extractedMimeType)
                && (preparedAttributes.containerXmlns() == null
                    || !preparedAttributes.containerXmlns().contains("xmldatacontainer"))) {
            if (preparedAttributes.transformation() != null)
                throw new SigningParametersException(XSLT_NO_XDC);

            preparedAttributes = new EFormAttributes(null, null, null, null, null, null, false);
        }

        if (!plainXmlEnabled && (isXML(extractedMimeType) || isXDC(extractedMimeType))
                && preparedAttributes.transformation() == null)
            throw new UnknownEformException();

        return new PreparedEFormAttributes(preparedAttributes, extractedMimeType);
    }

    public record PreparedEFormAttributes(EFormAttributes eFormAttributes, MimeType mimeType) {
    }

    private static DSSDocument normalize(DSSDocument dssDocument) {
        var mimeType = dssDocument.getMimeType();
        var name = dssDocument.getName();

        if (name != null && name.endsWith(".xdcf")) {
            dssDocument.setMimeType(XML_DATACONTAINER_WITH_CHARSET);
        } else if (mimeType != null && (isXDC(mimeType)
                || isXML(mimeType) && XDCValidator.isXDCContent(dssDocument))) {
            dssDocument.setMimeType(XML_DATACONTAINER_WITH_CHARSET);
        } else if (mimeType != null && isTxt(mimeType)) {
            dssDocument.setMimeType(TEXT_WITH_CHARSET);
        }

        return dssDocument;
    }
}