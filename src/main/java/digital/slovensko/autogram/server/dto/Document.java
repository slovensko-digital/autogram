package digital.slovensko.autogram.server.dto;
import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import java.util.Base64;
public class Document {
    private String filename;
    private String content;
    private String mimeType;
    private XDCParameters xdcParameters;

    public Document(String content) {
        this.content = content;
    }

    public Document(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public String getFilename() {
        if (filename == null || filename.isEmpty()) {
            return "document" + getMimeType().getExtension();
        }

        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getMimeTypeString() {
        return mimeType;
    }

    public MimeType getMimeType() {
        return AutogramMimeType.fromMimeTypeString(mimeType.split(";")[0]);
    }

    public XDCParameters getXdcParameters() {
        return xdcParameters;
    }

    public EFormAttributes getEformAttributes(String canonicalizationMethod, DigestAlgorithm digestAlgorithm) {
        if (getXdcParameters() == null)
            return null;

        return getXdcParameters().getEFormAttributes(canonicalizationMethod, digestAlgorithm);
    }

    public DSSDocument getDSSDocument() {
        var contentBytes = getContent().getBytes();
        if (mimeType.toLowerCase().contains("base64")) 
            contentBytes = Base64.getDecoder().decode(contentBytes);

        return new InMemoryDocument(contentBytes, getFilename(), getMimeType());
    }

    public AutogramDocument toAutogramDocument(String canonicalizationMethod, DigestAlgorithm digestAlgorithm) {
        return AutogramDocument.build(getDSSDocument(), getEformAttributes(canonicalizationMethod, digestAlgorithm));

    }
}
