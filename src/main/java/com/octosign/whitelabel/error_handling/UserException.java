package com.octosign.whitelabel.error_handling;

public class UserException extends SignerException {

    private final String header;

    public UserException(String header, String description, Throwable cause) {
        super(description, cause);
        this.header = header;
    }

    public UserException(String description) {
        this(null, description, null);
    }

    public UserException(String header, String description) {
        this(header, description, null);
    }

    public UserException(String description, Throwable cause) {
        this(null, description, cause);
    }

    public String getHeader() {
        return header;
    }

    public String getDescription() {
        return getMessage();
    }
}
