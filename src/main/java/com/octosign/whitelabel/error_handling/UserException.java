package com.octosign.whitelabel.error_handling;

import com.octosign.whitelabel.communication.CommunicationError;

public class UserException extends SignerException {

    public UserException() {};
    public UserException(String message) { super(message); }
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
