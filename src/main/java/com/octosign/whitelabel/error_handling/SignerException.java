package com.octosign.whitelabel.error_handling;

import com.octosign.whitelabel.communication.CommunicationError;

public class SignerException extends Exception {

    public SignerException() { this(null, null); }
    public SignerException(String message) { this(message, null); }
    public SignerException(Throwable cause) { this(null, cause); }
    public SignerException(String message, Throwable cause) {
        super(message, cause);
        System.out.println("Exception!" + message + cause);

    }
}
