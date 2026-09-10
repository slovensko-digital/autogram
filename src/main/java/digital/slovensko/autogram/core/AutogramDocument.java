package digital.slovensko.autogram.core;

import java.io.File;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.TEXT_WITH_CHARSET;
import static digital.slovensko.autogram.core.AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET;
import static digital.slovensko.autogram.core.AutogramMimeType.isTxt;
import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;

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