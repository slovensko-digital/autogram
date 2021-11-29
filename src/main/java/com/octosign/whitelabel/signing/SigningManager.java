package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.communication.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureParameters;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.octosign.whitelabel.communication.SignatureParameters.Format.*;

public class SigningManager {

    private Certificate activeCertificate;

    public Certificate getActiveCertificate() {
        return activeCertificate;
    }

    public void setActiveCertificate(Certificate certificate) {
        this.activeCertificate = certificate;
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
        if (format.equals(XADES))
            document.setName(parameters.getFilename());

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
        if (activeCertificate == null)
            throw new RuntimeException("Signing certificate not set");

        var commonCertificateVerifier = new CommonCertificateVerifier();
        DSSDocument signedDocument;
        var format = inputParameters.getFormat();

        if (format.equals(PADES)) {
            var parameters = SignatureParameterMapper.mapPAdESParameters(inputParameters);
            injectPrivateKeyData(parameters);

            var service = new PAdESService(commonCertificateVerifier);
            var dataToSign = service.getDataToSign(document, parameters);
            var token = getActiveCertificate().getToken().dssToken;
            var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), activeCertificate.getDssPrivateKey());

            signedDocument = service.signDocument(document, parameters, signatureValue);

        } else if (format.equals(XADES)) {
            var parameters = SignatureParameterMapper.mapXAdESParameters(inputParameters);
            injectPrivateKeyData(parameters);

            var service = new ASiCWithXAdESService(commonCertificateVerifier);
            var dataToSign = service.getDataToSign(document, parameters);
            var token = getActiveCertificate().getToken().dssToken;
            var signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), activeCertificate.getDssPrivateKey());

            signedDocument = service.signDocument(document, parameters, signatureValue);

        } else {
            throw new IntegrationException(Code.UNSUPPORTED_FORMAT, "Unsupported format: " + format);
        }

        return signedDocument;
    }

    private <T extends AbstractSignatureParameters<?>> void injectPrivateKeyData(T parameters) {
        parameters.setSigningCertificate(activeCertificate.getDssPKCertificate());
        parameters.setCertificateChain(activeCertificate.getDssPrivateKey().getCertificateChain());
    }
}
