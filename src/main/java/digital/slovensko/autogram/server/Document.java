package digital.slovensko.autogram.server;

public class Document {
    private String filename;
    private String content;

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
}
