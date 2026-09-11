package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.xdc.XDCBuilder;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.TEXT_WITH_CHARSET;
import static digital.slovensko.autogram.core.AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET;
import static digital.slovensko.autogram.core.AutogramMimeType.isTxt;
import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;
import static digital.slovensko.autogram.core.AutogramMimeType.isAsice;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.WRONG_MIME_TYPE;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.XSLT_NO_XDC;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_MIME_TYPE;
import static digital.slovensko.autogram.util.DSSUtils.getXdcfFilename;

public class AutogramDocument {
    private final DSSDocument dssDocument;
    private final EFormAttributes eFormAttributes;

    private AutogramDocument(DSSDocument dssDocument) {
        this(dssDocument, null);
    }

    private AutogramDocument(DSSDocument dssDocument, EFormAttributes eFormAttributes) {
        this.dssDocument = dssDocument;
        this.eFormAttributes = eFormAttributes;
    }

    public static AutogramDocument build(DSSDocument document, EFormAttributes eFormAttributes) {
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
                && !isXML(extractedMimeType) && !isXDC(extractedMimeType))
            throw new SigningParametersException(WRONG_MIME_TYPE);

        if (isXDC(extractedMimeType) || isXML(extractedMimeType))
            XDCValidator.validateXml(preparedAttributes.schema(), preparedAttributes.transformation(), extractedDocument,
                preparedAttributes.propertiesCanonicalization(), preparedAttributes.digestAlgorithm(), preparedAttributes.embedUsedSchemas());

        if (!isXDC(extractedMimeType)
                && (preparedAttributes.containerXmlns() == null
                    || !preparedAttributes.containerXmlns().contains("xmldatacontainer"))) {
            if (preparedAttributes.transformation() != null)
                throw new SigningParametersException(XSLT_NO_XDC);

            preparedAttributes = new EFormAttributes(null, null, null, null, null, null, false, null, false, null, null);
        }

        if (preparedAttributes != null && preparedAttributes.shouldCreateXdc() && !isXDC(document.getMimeType()) && !isAsice(document.getMimeType()))
            document = XDCBuilder.transform(preparedAttributes, preparedAttributes.propertiesCanonicalization(),
                    preparedAttributes.digestAlgorithm(), document.getName(), EFormUtils.getXmlFromDocument(document));

        if (isXDC(document.getMimeType())) {
            document.setMimeType(AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET);
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