package digital.slovensko.autogram.core;

public class ExtractedFile {
    private final byte[] content;
    private final String filename;

    public ExtractedFile(String filename, byte[] content) {
        this.content = content;
        this.filename = filename;
    }

    public byte[] getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }
}
