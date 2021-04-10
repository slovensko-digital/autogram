package com.octosign.whitelabel.signing;

import eu.europa.esig.dss.token.MSCAPISignatureToken;

public class SigningCertificateMSCAPI extends SigningCertificate {
    /**
     * Create signing certificate that will use Windows MS CAPI
     */
    public SigningCertificateMSCAPI() {
        try {
            token = new MSCAPISignatureToken();
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize MSCAPI", e);
        }
    }
}
