package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.Selectable;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.text.SimpleDateFormat;

/**
 * Represents a combination of Token and PrivateKey within that token
 *
 * - Can retrieve a list of possible keys within the current.
 * - Can construct human readable description of the key.
 * - Can sign an XML document represented as string.
 */
public abstract class Certificate implements Selectable {
    /**
     * Token set by technology-specific class (MSCAPI/PKCS11/PKCS12)
     */
    protected Token token;

    protected DSSPrivateKeyEntry dssPrivateKey;

    /**
     * How verbose should key description be
     * - LONG - Contains name, address, and date range
     * - SHORT - Contains name and date range
     * - NAME - Contains name only
     */
    public enum DescriptionVerbosity {
        LONG, SHORT, NAME
    }

    /**
     * Constructs human readable private key description
     */
    public static String getCertificateDescription(Certificate certificate, DescriptionVerbosity verbosity) {
        String dn = certificate.getDssPKCertificate().getSubject().getRFC2253();
        String label = "";
        try {
            LdapName ldapDN = new LdapName(dn);
            String dnName = "";
            String dnCountry = "";
            String dnCity = "";
            String dnStreet = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String notBefore = dateFormat.format(certificate.getDssPKCertificate().getNotBefore());
            String notAfter = dateFormat.format(certificate.getDssPKCertificate().getNotAfter());
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
            } else {
                label = dnName;
            }
        } catch (Exception e) {
            // If retrieving sensible name fails, use serial number
            label = "SN: " + certificate.getDssPKCertificate().getCertificate().getSerialNumber().toString(16);
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
    public String getName() {
        return this.getCertificateDescription(DescriptionVerbosity.NAME);
    }

    public void setDssPrivateKey(DSSPrivateKeyEntry dssPrivateKey) {
        this.dssPrivateKey = dssPrivateKey;
    }

    public DSSPrivateKeyEntry getDssPrivateKey() {
        return dssPrivateKey;
    }

    protected Token getToken() {
        return token;
    }

    protected CertificateToken getDssPKCertificate() {
        if (dssPrivateKey == null)
            throw new RuntimeException("Private key not set up");

        return dssPrivateKey.getCertificate();
    }
}
