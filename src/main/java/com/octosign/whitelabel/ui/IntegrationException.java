package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.CommunicationError;

public class IntegrationException extends SignerException {
    public IntegrationException(String message) { super(message); }
    public IntegrationException(String message, Throwable cause) { super(message, cause); }
    public IntegrationException(String message, CommunicationError.Code code) { super(message, code); }
    public IntegrationException(String message, CommunicationError.Code code, Throwable cause) { super(message, code, cause); }

}
