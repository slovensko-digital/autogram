package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
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
import static com.octosign.whitelabel.ui.I18n.translate;

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
    public enum Verbosity {
        LONG,
        SHORT,
        NAME
    }

    /**
     * Constructs human readable private key description
     */
    public static String getNicePrivateKeyDescription(DSSPrivateKeyEntry key, Verbosity verbosity) {
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

            if (verbosity == Verbosity.LONG) {
                label = String.format("%s, %s %s, %s (%s - %s)", dnName, dnCity, dnStreet, dnCountry, notBefore,
                    notAfter);
            } else if (verbosity == Verbosity.SHORT) {
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
        return token.getKeys();
    }

    /**
     * Constructs human readable description for the current private key
     */
    public String getNicePrivateKeyDescription(Verbosity verbosity) {
        return SigningCertificate.getNicePrivateKeyDescription(privateKey, verbosity);
    }

    /**
     * Signs passed UTF-8 encoded string document and returns document in the same format
     */
    public String sign(SignatureUnit unit) {
        var parameters = unit.getSignatureParameters();
        var format = parameters.getFormat();

        if (format.equals(XADES))
            unit.toXDC();
        var content = unit.getDocument().getContent();

        byte[] binaryContent;
        if (format.equals(PADES)) {
            binaryContent = Base64.getDecoder().decode(content);
        } else {
            binaryContent = content.getBytes(StandardCharsets.UTF_8);
        }

        var document = new InMemoryDocument(binaryContent);
        document.setName(parameters.getFilename());

        // TODO shouldn't this apply only for XAdES?
        document.setMimeType(MimeType.fromMimeTypeString("application/vnd.gov.sk.xmldatacontainer+xml; charset=UTF-8"));

        var signedDocument = sign(document, parameters);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            signedDocument.writeTo(output);
        } catch (IOException e) {
            throw new IntegrationException(Code.STREAM_NOT_AVAILABLE, e);
        }

        return Utils.toBase64(output.toByteArray());
    }

    private DSSDocument sign(CommonDocument document, SignatureParameters inputParameters) {
        if (privateKey == null)
            throw new RuntimeException(translate("error.missingPrivateKey"));

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
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

            var service = new ASiCWithXAdESService(commonCertificateVerifier);
            var dataToSign = service.getDataToSign(document, parameters);
            var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
            assert(service.isValidSignatureValue(dataToSign, signatureValue, privateKey.getCertificate()));

            signedDocument = service.signDocument(document, parameters, signatureValue);

        } else {
            throw new IllegalArgumentException(translate("error.unsupportedFormat_", format));
        }

        return signedDocument;
    }
}
