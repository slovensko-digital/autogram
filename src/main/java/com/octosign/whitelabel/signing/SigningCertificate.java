package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.communication.*;
import com.octosign.whitelabel.error_handling.*;

import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.octosign.whitelabel.communication.SignatureParameters.Format.*;


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
            throw new UserException("error.tokenNotAvailable.header", "error.tokenNotAvailable.description", e);
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
    public String sign(SignatureUnit data) {
        var parameters = data.getSignatureParameters();
        var isXAdES = parameters.getFormat().equals(XADES);

        if (isXAdES) data.transformToXDC();

        var document = new InMemoryDocument(data.getBinaryContent());

        if (isXAdES) {
            document.setName(parameters.getContainerFilename());
            document.setMimeType(MimeType.fromMimeTypeString(parameters.getFileMimeType()));
        }

        var signedDocument = sign(document, parameters);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            signedDocument.writeTo(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Utils.toBase64(output.toByteArray());
    }

    private DSSDocument sign(CommonDocument document, SignatureParameters inputParameters) {
        if (privateKey == null)
            throw new RuntimeException("Signing certificate not set");

        var commonCertificateVerifier = new CommonCertificateVerifier();
        DSSDocument signedDocument;
        var format = inputParameters.getFormat();

        if (format.equals(PADES)) {
            var parameters = SignatureParameterMapper.mapPAdESParameters(inputParameters);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setCertificateChain(privateKey.getCertificateChain());

            var service = new PAdESService(commonCertificateVerifier);
            var dataToSign = service.getDataToSign(document, parameters);
            var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);

            signedDocument = service.signDocument(document, parameters, signatureValue);

        } else if (format.equals(XADES)) {
            var parameters = SignatureParameterMapper.mapXAdESParameters(inputParameters);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setCertificateChain(privateKey.getCertificateChain());

            if (parameters instanceof ASiCWithXAdESSignatureParameters asicParameters) {
                var service = new ASiCWithXAdESService(commonCertificateVerifier);
                var dataToSign = service.getDataToSign(document, asicParameters);
                var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);

                signedDocument = service.signDocument(document, asicParameters, signatureValue);

            } else {
                var service = new XAdESService(commonCertificateVerifier);
                var dataToSign = service.getDataToSign(document, parameters);
                var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);

                signedDocument = service.signDocument(document, parameters, signatureValue);
            }
        } else {
            throw new IntegrationException(Code.UNSUPPORTED_FORMAT, "Unsupported format: " + format);
        }

        return signedDocument;
    }
}
