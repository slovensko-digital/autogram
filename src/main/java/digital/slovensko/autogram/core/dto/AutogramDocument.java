package digital.slovensko.autogram.core.dto;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.xdc.XDCBuilder;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import digital.slovensko.autogram.util.PDFUtils;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.WRONG_MIME_TYPE;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.XSLT_NO_XDC;
import static digital.slovensko.autogram.core.dto.AutogramMimeType.TEXT_WITH_CHARSET;
import static digital.slovensko.autogram.core.dto.AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_MIME_TYPE;
import static digital.slovensko.autogram.util.DSSUtils.getXdcfFilename;

import java.util.List;

public class AutogramDocument {
    private final DSSDocument dssDocument;
    private final EFormAttributes eFormAttributes;

    private AutogramDocument(DSSDocument dssDocument) {
        this(dssDocument, null);
    }

    private AutogramDocument(DSSDocument dssDocument, EFormAttributes eFormAttributes) {
        this.dssDocument = dssDocument;
        
        if (eFormAttributes == null)
            this.eFormAttributes = EFormAttributes.build(null, null);
        
        else
            this.eFormAttributes = eFormAttributes;
    }

    public static AutogramDocument build(DSSDocument document, EFormAttributes eFormAttributes) {
        return build(document, eFormAttributes, false);
    }

    public static AutogramDocument build(DSSDocument document, EFormAttributes eFormAttributes, boolean preserveFileName) {
        if (document.getMimeType() == null)
            throw new SigningParametersException(NO_MIME_TYPE);

        document = normalize(document);

        var isAsiceDocument = AutogramMimeType.isAsice(document.getMimeType());
        var extractedDocument = document;
        if (isAsiceDocument)
            extractedDocument = normalize(AsicContainerUtils.getOriginalDocuments(document).get(0));

        var preparedAttributes = EFormAttributes.build(eFormAttributes, extractedDocument);
        var extractedMimeType = extractedDocument.getMimeType();

        if (preparedAttributes.containerXmlns() != null
                && preparedAttributes.containerXmlns().contains("xmldatacontainer")
                && !AutogramMimeType.isXML(extractedMimeType) && !AutogramMimeType.isXDC(extractedMimeType))
            throw new SigningParametersException(WRONG_MIME_TYPE);

        if (AutogramMimeType.isXDC(extractedMimeType) || AutogramMimeType.isXML(extractedMimeType))
            XDCValidator.validateXml(preparedAttributes.schema(), preparedAttributes.transformation(), extractedDocument,
                preparedAttributes.propertiesCanonicalization(), preparedAttributes.digestAlgorithm(), preparedAttributes.embedUsedSchemas());

        if (!AutogramMimeType.isXDC(extractedMimeType)
                && (preparedAttributes.containerXmlns() == null
                    || !preparedAttributes.containerXmlns().contains("xmldatacontainer"))) {
            if (preparedAttributes.transformation() != null)
                throw new SigningParametersException(XSLT_NO_XDC);

            preparedAttributes = eFormAttributes;
        }

        if (preparedAttributes != null && preparedAttributes.shouldCreateXdc() && !AutogramMimeType.isXDC(document.getMimeType()) && !AutogramMimeType.isAsice(document.getMimeType()))
            document = XDCBuilder.transform(preparedAttributes, preparedAttributes.propertiesCanonicalization(),
                    preparedAttributes.digestAlgorithm(), document.getName(), EFormUtils.getXmlFromDocument(document));

        if (AutogramMimeType.isXDC(document.getMimeType())) {
            document.setMimeType(AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET);

            if (!preserveFileName)
                document.setName(getXdcfFilename(document.getName()));
        }

        return new AutogramDocument(document, preparedAttributes);
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

    public boolean isEForm() {
        return eFormAttributes != null && eFormAttributes.shouldCreateXdc()
                && eFormAttributes.identifier() != null && !eFormAttributes.identifier().isEmpty();
    }

    public boolean isPDF() {
        return dssDocument.getMimeType() != null && AutogramMimeType.isPDF(dssDocument.getMimeType());
    }

    public boolean isXML() {
        return dssDocument.getMimeType() != null && AutogramMimeType.isXML(dssDocument.getMimeType());
    }

    public boolean isXDC() {
        return dssDocument.getMimeType() != null && AutogramMimeType.isXDC(dssDocument.getMimeType());
    }

    public boolean isAsice() {
        return AutogramMimeType.isAsice(dssDocument.getMimeType());
    }

    public boolean isPDFAndPasswordProtected() {
        return isPDF() && PDFUtils.isPdfAndPasswordProtected(dssDocument);
    }

    public List<DSSDocument> getOriginalDocuments() {
        if (!isAsice())
            return List.of(dssDocument);

        return AsicContainerUtils.getOriginalDocuments(dssDocument);
    }

    private static DSSDocument normalize(DSSDocument dssDocument) {
        var mimeType = dssDocument.getMimeType();
        var name = dssDocument.getName();

        if (name != null && name.endsWith(".xdcf")) {
            dssDocument.setMimeType(XML_DATACONTAINER_WITH_CHARSET);
        } else if (mimeType != null && (AutogramMimeType.isXDC(mimeType)
                || AutogramMimeType.isXML(mimeType) && XDCValidator.isXDCContent(dssDocument))) {
            dssDocument.setMimeType(XML_DATACONTAINER_WITH_CHARSET);
        } else if (mimeType != null && AutogramMimeType.isTxt(mimeType)) {
            dssDocument.setMimeType(TEXT_WITH_CHARSET);
        }

        return dssDocument;
    }
}