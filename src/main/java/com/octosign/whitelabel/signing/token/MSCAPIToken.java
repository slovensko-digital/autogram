package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.*;
import eu.europa.esig.dss.token.MSCAPISignatureToken;

public class MSCAPIToken extends Token {
    /**
     * Create signing certificate that will use Windows MS CAPI
     */
    public MSCAPIToken() {
        MSCAPISignatureToken token;
        try {
             token = new MSCAPISignatureToken();
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
        initialize(token);
    }
}
