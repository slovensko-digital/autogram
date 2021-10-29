package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.communication.SignatureUnit;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;

import static com.octosign.whitelabel.communication.SignatureParameters.Format.PADES;
import static com.octosign.whitelabel.communication.SignatureParameters.Format.XADES;

/**
 * Represents a combination of Token and PrivateKey within that token
 *
 * - Can retrieve a list of possible keys within the current.
 * - Can construct human readable description of the key.
 * - Can sign an XML document represented as string.
 */
public abstract class SigningCertificate {
    /**
     * Token set by technology-specific class (MSCAPI/PKCS11/PKCS12)
     */
    protected AbstractKeyStoreTokenConnection token;

    /**
     * Private key that has to be set using .setPrivateKey() before signing
     *
     * List of available private keys can be retrieved using .getAvailablePrivateKeys()
     */
    private DSSPrivateKeyEntry privateKey;

    /**
     * How verbose should key description be
     * - LONG - Contains name, address, and date range
     * - SHORT - Contains name and date range
     * - NAME - Contains name only
     */
    public enum KeyDescriptionVerbosity {
        LONG,
        SHORT,
        NAME
    }

    /**
     * Constructs human readable private key description
     */
    public static String getNicePrivateKeyDescription(DSSPrivateKeyEntry key, KeyDescriptionVerbosity verbosity) {
        String dn = key.getCertificate().getSubject().getRFC2253();
        String label = "";
        try {
            LdapName ldapDN = new LdapName(dn);
            String dnName = "";
            String dnCountry = "";
            String dnCity = "";
            String dnStreet = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String notBefore = dateFormat.format(key.getCertificate().getNotBefore());
            String notAfter = dateFormat.format(key.getCertificate().getNotAfter());
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

            if (verbosity == KeyDescriptionVerbosity.LONG) {
                label = String.format("%s, %s %s, %s (%s - %s)", dnName, dnCity, dnStreet, dnCountry, notBefore,
                    notAfter);
            } else if (verbosity == KeyDescriptionVerbosity.SHORT) {
                label = String.format("%s (%s - %s)", dnName, notBefore, notAfter);
            } else {
                label = dnName;
            }
        } catch (Exception e) {
            // If retrieving sensible name fails, use serial number
            label = "SN: " + key.getCertificate().getCertificate().getSerialNumber().toString(16);
        }

        return label;
    }

    /**
     * Set private key used for signing
     *
     * @param privateKey
     */
    public void setPrivateKey(DSSPrivateKeyEntry privateKey) {
        this.privateKey = privateKey;
    }

    public List<DSSPrivateKeyEntry> getAvailablePrivateKeys() {
        List<DSSPrivateKeyEntry> keys;
        try {
            keys = token.getKeys();
        } catch (Exception e) {
            throw new RuntimeException(translate("error.keysNotRetrieved", e));
        }

        return keys;
    }

    /**
     * Constructs human readable description for the current private key
     */
    public String getNicePrivateKeyDescription(KeyDescriptionVerbosity verbosity) {
        return SigningCertificate.getNicePrivateKeyDescription(privateKey, verbosity);
    }

    /**
     * Signs passed UTF-8 encoded string document and returns document in the same format
     */
    public String sign(SignatureUnit unit) throws IOException {
        var content = unit.getDocument().getContent();
        var parameters = unit.getSignatureParameters();

        byte[] binaryContent;
        if (parameters.getFormat() == PADES) {
            binaryContent = Base64.getDecoder().decode(content);
        } else {
            if (parameters.getFormat() == XADES) unit.standardizeAsXDC();
            binaryContent = content.getBytes(StandardCharsets.UTF_8);
        }

        var document = new InMemoryDocument(binaryContent);
        document.setName(parameters.getFilename());

        var output = new ByteArrayOutputStream();
        var signedDocument = sign(document, parameters);
        signedDocument.writeTo(output);

        return Utils.toBase64(output.toByteArray());
    }

    private DSSDocument sign(CommonDocument document, SignatureParameters inputParameters) {
        if (privateKey == null)
            throw new RuntimeException("Missing private key");

        var commonCertificateVerifier = new CommonCertificateVerifier();

        switch(inputParameters.getFormat()) {
            case XADES -> {
                var parameters = SignatureParameterMapper.mapXAdESParameters(inputParameters);
                parameters.setSigningCertificate(privateKey.getCertificate());
                parameters.setCertificateChain(privateKey.getCertificateChain());

                var service = new ASiCWithXAdESService(commonCertificateVerifier);
                var dataToSign = service.getDataToSign(document, parameters);
                var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
                assert(service.isValidSignatureValue(dataToSign, signatureValue, privateKey.getCertificate()));

                return service.signDocument(document, parameters, signatureValue);
            }

            case PADES -> {
                var parameters = SignatureParameterMapper.mapPAdESParameters(inputParameters);
                parameters.setSigningCertificate(privateKey.getCertificate());
                parameters.setCertificateChain(privateKey.getCertificateChain());

                var service = new PAdESService(commonCertificateVerifier);
                var dataToSign = service.getDataToSign(document, parameters);
                var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
                assert(service.isValidSignatureValue(dataToSign, signatureValue, privateKey.getCertificate()));

                return service.signDocument(document, parameters, signatureValue);
            }

            default -> throw new IllegalArgumentException("Unknown format");
        }
    }
}
