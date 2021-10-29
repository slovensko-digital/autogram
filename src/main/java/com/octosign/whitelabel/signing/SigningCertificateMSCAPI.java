package com.octosign.whitelabel.signing;

import eu.europa.esig.dss.token.MSCAPISignatureToken;

import static com.octosign.whitelabel.ui.Main.translate;

public class SigningCertificateMSCAPI extends SigningCertificate {
    /**
     * Create signing certificate that will use Windows MS CAPI
     */
    public SigningCertificateMSCAPI() {
        try {
            token = new MSCAPISignatureToken();
        } catch (Exception e) {
            throw new RuntimeException(translate("error.mscapiInitFailed", e));
        }
    }
}
