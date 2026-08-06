package digital.slovensko.autogram.server.dto;

public class Document {
    private String filename;
    private String content;
    private String mimeType;
    private XDCParameters xdcParameters;
    private VisibleSignature visibleSignature;

    public Document(String content) {
        this.content = content;
    }

    public Document(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public XDCParameters getXdcParameters() {
        return xdcParameters;
    }

    public VisibleSignature getVisibleSignature() {
        return visibleSignature;
    }
}
