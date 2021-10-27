package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.CommunicationError;

public class UserException extends SignerException {

    public UserException(String message, CommunicationError.Code code) {
        super(message, code);
    }


    public UserException(String message) {
        super(message);
    }

    public UserException(String message, CommunicationError.Code code, Throwable cause) {
        super(message, code, cause);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
