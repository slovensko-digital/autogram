package digital.slovensko.autogram.server.dto;

public class SignResponse {
    private String content;
    private String signedBy;
    private String issuedBy;

    public SignResponse(String content, String signedBy, String issuedBy) {
        this.content = content;
        this.signedBy = signedBy;
        this.issuedBy = issuedBy;
    }
}
