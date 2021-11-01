package com.octosign.whitelabel.error_handling;

import com.octosign.whitelabel.communication.CommunicationError;

public class UserException extends SignerException {

    private String header;

    public UserException(String description) { this(null, description, null); }
    public UserException(String description, Throwable cause) { this(null, description, cause); }
    public UserException(String header, String description) { this(header, description, null); }
    public UserException(String header, String description, Throwable cause) {
        super(description, cause);
        this.header = header;
    }

    public String getHeader() { return header; }
}
