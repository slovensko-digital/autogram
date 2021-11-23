package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.ui.PasswordCallback;

import javax.swing.*;
import java.io.File;
import java.util.function.Supplier;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;
import static com.octosign.whitelabel.ui.I18n.translate;

public class Driver  {
    public enum Type {
        EID() {
            @Override
            public Driver getDriver() {
                return new Driver(StorageType.SINGLE_KEY, translate("btn.eidCert"), getProperty("driver.pkcs11.eidCert.linux"));
            }
        },

        MANDATE() {
            @Override
            public Driver getDriver() {
                return new Driver(StorageType.MULTIPLE_KEYS, translate("btn.mandateCert"), getProperty("driver.pkcs11.mandateCert.linux"));
            }
        };

        public abstract Driver getDriver();
    }

    public enum StorageType {
        SINGLE_KEY,
        MULTIPLE_KEYS;
    }

    private StorageType type;
    private String name;
    private String path;
    private SigningCertificate certificate;

    public Driver(StorageType type, String name, String path) {
        this.type = type;
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public StorageType getType() {
        return type;
    }

    public SigningCertificate getCertificate() {
        if (certificate == null)
            certificate = CertificateFactory.create(this);

        return certificate;
    }

    public boolean singlePrivateKey() {
        if (certificate == null || certificate.getAvailablePrivateKeys().size() == 0)
            throw new UserException("");

        return certificate.getAvailablePrivateKeys().size() == 1;
    }
}

