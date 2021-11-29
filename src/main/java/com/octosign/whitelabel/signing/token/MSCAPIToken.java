package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.Token;
import eu.europa.esig.dss.token.MSCAPISignatureToken;

public class MSCAPIToken extends Token {
    /**
     * Create signing certificate that will use Windows MS CAPI
     */
    public MSCAPIToken() {
        try {
            dssToken = new MSCAPISignatureToken();
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
    }
}
