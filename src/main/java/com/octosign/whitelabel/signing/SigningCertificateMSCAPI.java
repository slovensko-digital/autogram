package com.octosign.whitelabel.signing;

import eu.europa.esig.dss.token.MSCAPISignatureToken;

import static com.octosign.whitelabel.ui.I18n.translate;

public class SigningCertificateMSCAPI extends SigningCertificate {
    /**
     * Create signing certificate that will use Windows MS CAPI
     */
    public SigningCertificateMSCAPI() {
        token = new MSCAPISignatureToken();
    }
}
