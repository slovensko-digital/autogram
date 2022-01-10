package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.signing.token.Token;
import com.octosign.whitelabel.ui.picker.SelectableItem;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.text.SimpleDateFormat;

import static java.util.Objects.requireNonNull;

/**
 * Represents a combination of Token and PrivateKey within that token
 */
public class Certificate implements SelectableItem {
    /**
     * Token set by technology-specific class (MSCAPI/PKCS11/PKCS12)
     */
    private final Token token;

    private final DSSPrivateKeyEntry dssPrivateKey;

    public Certificate(DSSPrivateKeyEntry dssPrivateKey, Token token) {
        this.dssPrivateKey = requireNonNull(dssPrivateKey);
        this.token = requireNonNull(token);
    }

    protected DSSPrivateKeyEntry getDssPrivateKey() {
        return dssPrivateKey;
    }

    protected Token getToken() {
        return token;
    }

    /**
     * How verbose should key description be
     * - LONG - Contains name, address, and date range
     * - SHORT - Contains name and date range
     * - NAME - Contains name only
     */
    public enum DescriptionVerbosity {
        LONG,
        SHORT,
        COMPACT,
        NAME
    }

    /**
     * Constructs human readable private key description
     */
    public static String getCertificateDescription(Certificate certificate, DescriptionVerbosity verbosity) {
        var privateKey = certificate.getDssPrivateKey();
        String dn = privateKey.getCertificate().getSubject().getRFC2253();
        String label = "";
        try {
            LdapName ldapDN = new LdapName(dn);
            String dnName = "";
            String dnCountry = "";
            String dnCity = "";
            String dnStreet = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String notBefore = dateFormat.format(privateKey.getCertificate().getNotBefore());
            String notAfter = dateFormat.format(privateKey.getCertificate().getNotAfter());
            for (Rdn rdn: ldapDN.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN"))
                    dnName = rdn.getValue().toString();
                if (rdn.getType().equalsIgnoreCase("C"))
                    dnCountry = rdn.getValue().toString();
                if (rdn.getType().equalsIgnoreCase("L"))
                    dnCity = rdn.getValue().toString();
                if (rdn.getType().equalsIgnoreCase("STREET"))
                    dnStreet = rdn.getValue().toString();
            }

            if (verbosity == DescriptionVerbosity.LONG) {
                label = String.format("%s, %s %s, %s (%s - %s)", dnName, dnCity, dnStreet, dnCountry, notBefore,
                    notAfter);
            } else if (verbosity == DescriptionVerbosity.SHORT) {
                label = String.format("%s (%s - %s)", dnName, notBefore, notAfter);
            } else if (verbosity == DescriptionVerbosity.COMPACT) {
                label = dnName;
            } else {
                label = dnName;
            }
        } catch (Exception e) {
            // If retrieving sensible name fails, use serial number
            label = "SN: " + privateKey.getCertificate().getCertificate().getSerialNumber().toString(16);
        }

        return label;
    }

    /**
     * Constructs human readable description for the current private key
     */
    public String getCertificateDescription(DescriptionVerbosity verbosity) {
        return getCertificateDescription(this, verbosity);
    }

    @Override
    public String getDisplayedName() {
        return this.getCertificateDescription(DescriptionVerbosity.NAME);
    }

    @Override
    public String getDisplayedDetails() {
        return this.getCertificateDescription(DescriptionVerbosity.COMPACT);
    }
}
