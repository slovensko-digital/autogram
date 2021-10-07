package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.communication.SignatureUnit;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.communication.SignatureUnit;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.xades.signature.XAdESService;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;

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
            throw new RuntimeException("Private keys could not be retrieved", e);
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
        if (unit.getSignatureParameters().getFormat().equals(SignatureParameters.Format.XADES))
            unit.standardizeAsXDC();

        var content = unit.getDocument().getContent();

        byte[] binaryContent;
        if (unit.getSignatureParameters().getFormat().equals(SignatureParameters.Format.PADES)) {
            binaryContent = Base64.getDecoder().decode(content);
        } else {
            binaryContent = content.getBytes(StandardCharsets.UTF_8);
        }

        var document = new InMemoryDocument(binaryContent);
        // TODO!
        document.setName("Vseobecna_agenda.xml");
        document.setMimeType(MimeType.fromMimeTypeString("application/vnd.gov.sk.xmldatacontainer+xml; charset=UTF-8"));

        var signedDocument = sign(document, unit.getSignatureParameters());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        signedDocument.writeTo(output);

        return Utils.toBase64(output.toByteArray());
    }
    // TODO update these 2 sign methods appropriately to emerging needs after adding new signature types
    private DSSDocument sign(CommonDocument document, SignatureParameters inputParameters) {
        if (privateKey == null) {
            throw new RuntimeException("Missing private key");
        }

        // TODO: Add trust for -LT/-LTA - requires use of qualified services
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        var parameters = SignatureParameterMapper.map(inputParameters);
        parameters.setSigningCertificate(privateKey.getCertificate());
        parameters.setCertificateChain(privateKey.getCertificateChain());

        DSSDocument signedDocument = null;

        if (inputParameters.getFormat().equals(SignatureParameters.Format.PADES)) {
            var service = new PAdESService(commonCertificateVerifier);

            // only if signature image is set!
            // service.setPdfObjFactory(new PdfBoxNativeObjectFactory());

            // TODO: Add support for TSP
        /*
        boolean useTsp = false;
        // We choose the level of the signature (-B, -T, -LT, -LTA).
        parameters.setSignatureLevel(useTsp ? SignatureLevel.XAdES_BASELINE_T : SignatureLevel.XAdES_BASELINE_B);
        if (useTsp) {
            // Create and set the TSP source
            OnlineTSPSource tspSource = new OnlineTSPSource(tspUrl);
            service.setTspSource(tspSource);
        }*/

            var padesParameters = (PAdESSignatureParameters) parameters;
            // Get the SignedInfo segment that needs to be signed.
            ToBeSigned dataToSign = service.getDataToSign(document, padesParameters);

            // Create signature - digest - for the signed data
            SignatureValue signatureValue = token.sign(dataToSign, padesParameters.getDigestAlgorithm(), privateKey);

            // Use the signature to sign the document
            signedDocument = service.signDocument(document, padesParameters, signatureValue);

            System.out.println(signedDocument.getName());
            try {
                signedDocument.writeTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return signedDocument;
        }

        if (inputParameters.getFormat().equals(SignatureParameters.Format.XADES)) {
            var service = new ASiCWithXAdESService(commonCertificateVerifier);

        // TODO: Add support for TSP
        /*
        boolean useTsp = false;
        // We choose the level of the signature (-B, -T, -LT, -LTA).
        parameters.setSignatureLevel(useTsp ? SignatureLevel.XAdES_BASELINE_T : SignatureLevel.XAdES_BASELINE_B);
        if (useTsp) {
            // Create and set the TSP source
            OnlineTSPSource tspSource = new OnlineTSPSource(tspUrl);
            service.setTspSource(tspSource);
        }*/

            var xadesParameters = (ASiCWithXAdESSignatureParameters) parameters;

            // Get the SignedInfo segment that needs to be signed.
            ToBeSigned dataToSign = service.getDataToSign(document, xadesParameters);

            // Create signature - digest - for the signed data
            SignatureValue signatureValue = token.sign(dataToSign, xadesParameters.getDigestAlgorithm(), privateKey);

            // TODO optional validation - delete
            assert(service.isValidSignatureValue(dataToSign, signatureValue, privateKey.getCertificate()));

            // Use the signature to sign the document
            signedDocument = service.signDocument(document, xadesParameters, signatureValue);
        }

        return signedDocument;
    }
}
