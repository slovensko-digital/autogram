package digital.slovensko.autogram.server.dto;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.AutogramMimeType;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import java.util.Base64;
public record Document (String filename, String content, String mimeType, XDCParameters xdcParameters) {
    public Document(String content) {
        this(null, content, null, null);
    }

    public Document(String filename, String content) {
        this(filename, content, null, null);
    }

    public String getFilename() {
        if (filename == null || filename.isEmpty()) {
            return "document" + getMimeType().getExtension();
        }

        return filename;
    }

    public String getMimeTypeString() {
        return mimeType;
    }

    public MimeType getMimeType() {
        return AutogramMimeType.fromMimeTypeString(mimeType.split(";")[0]);
    }

    public boolean isBase64() {
        return mimeType != null && mimeType.toLowerCase().contains("base64");
    }

    private EFormAttributes getEformAttributes(String canonicalizationMethod, DigestAlgorithm digestAlgorithm) {
        if (xdcParameters() == null)
            return null;

        return xdcParameters().getEFormAttributes(canonicalizationMethod, digestAlgorithm, isBase64());
    }

    public DSSDocument getDSSDocument() {
        var contentBytes = content().getBytes();
        if (mimeType.toLowerCase().contains("base64")) 
            contentBytes = Base64.getDecoder().decode(contentBytes);

        return new InMemoryDocument(contentBytes, getFilename(), getMimeType());
    }

    public AutogramDocument toAutogramDocument(String canonicalizationMethod, DigestAlgorithm digestAlgorithm) {
        return AutogramDocument.build(getDSSDocument(), getEformAttributes(canonicalizationMethod, digestAlgorithm));

    }
}
