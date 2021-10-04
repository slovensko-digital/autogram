package com.octosign.whitelabel.communication.document;

/**
 * Generic document exchanged during communication
 */
public class Document implements Cloneable {
    protected String id;
    protected String title;
    protected String content;
    protected String legalEffect;

    public Document() {}

    public Document(String id, String title, String content, String legalEffect) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.legalEffect = legalEffect;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLegalEffect() {
        return legalEffect;
    }

    public void setLegalEffect(String legalEffect) {
        this.legalEffect = legalEffect;
    }

    @Override
    public Document clone() {
        return new Document(id, title, content, legalEffect);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", legalEffect='" + legalEffect + '\'' +
                '}';
    }
}
