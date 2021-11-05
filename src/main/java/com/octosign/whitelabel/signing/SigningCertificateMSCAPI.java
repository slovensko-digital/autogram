package com.octosign.whitelabel.signing;

import eu.europa.esig.dss.token.MSCAPISignatureToken;

public class SigningCertificateMSCAPI extends SigningCertificate {
    /**
     * Create signing certificate that will use Windows MS CAPI
     */
    public SigningCertificateMSCAPI() {
        token = new MSCAPISignatureToken();
    }
}
