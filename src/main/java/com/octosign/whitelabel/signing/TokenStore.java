package com.octosign.whitelabel.signing;

public interface TokenStore<T extends SigningCertificate> {
    String[] getDrivers();
}
