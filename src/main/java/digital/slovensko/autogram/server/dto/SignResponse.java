package digital.slovensko.autogram.server.dto;

public class SignResponse {
    private String content;
    private String mimeType;
    private String filename;
    private String signedBy;
    private String issuedBy;

    public SignResponse(String content, String mimeType, String filename, String signedBy, String issuedBy) {
        this.content = content;
        this.mimeType = mimeType;
        this.filename = filename;
        this.signedBy = signedBy;
        this.issuedBy = issuedBy;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public String getIssuedBy() {
        return issuedBy;
    }
}
