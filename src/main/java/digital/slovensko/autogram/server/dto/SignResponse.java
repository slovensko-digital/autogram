package digital.slovensko.autogram.server.dto;

public class SignResponse {
    private String content;
    private String signedBy;
    private String issuedby;

    public SignResponse(String content, String signedBy, String issuedby) {
        this.content = content;
        this.signedBy = signedBy;
        this.issuedby = issuedby;
    }
}
