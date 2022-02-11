package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.preprocessing.SignatureParameterMapper;
import com.octosign.whitelabel.communication.SignatureUnit;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.octosign.whitelabel.ui.utils.Utils.isPresent;

public class SigningManager {

    private Certificate activeCertificate;

    public Certificate getActiveCertificate() {
        return activeCertificate;
    }

    public void setActiveCertificate(Certificate certificate) {
        this.activeCertificate = certificate;
    }

    public byte[] sign(SignatureUnit data) {
        if (activeCertificate == null)
            throw new RuntimeException("Signing certificate not set");

        DSSDocument signedDocument;

        if (data.isPDF())
            signedDocument = signPAdES(data);
        else if (data.isXDC())
            signedDocument = signASiCWithXAdES(data);
        else if (data.isXML())
            signedDocument = signXAdES(data);
        else
            signedDocument = signCAdES(data);


        try (var output = new ByteArrayOutputStream()) {
            signedDocument.writeTo(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DSSDocument signASiCWithXAdES(SignatureUnit data) {
        data.transformToXDC();
        var params = data.getSignatureParameters();

        var dssDocument = new InMemoryDocument(data.getDocument().getContent());
        if (isPresent(params.getContainerFilename()))
            dssDocument.setName(params.getContainerFilename());
        if (params.getFileMimeType() != null)
            dssDocument.setMimeType(params.getFileMimeType().toDSSMimeType());

        var dssParams = SignatureParameterMapper.mapASiCWithXAdESParameters(params);
        dssParams.setSigningCertificate(activeCertificate.getDssPrivateKey().getCertificate());
        dssParams.setCertificateChain(activeCertificate.getDssPrivateKey().getCertificateChain());

        var service = new ASiCWithXAdESService(new CommonCertificateVerifier());
        var dataToSign = service.getDataToSign(dssDocument, dssParams);
        var token = getActiveCertificate().getToken().getDssToken();
        var signatureValue = token.sign(dataToSign, dssParams.getDigestAlgorithm(), activeCertificate.getDssPrivateKey());

       return service.signDocument(dssDocument, dssParams, signatureValue);
    }

    private DSSDocument signPAdES(SignatureUnit data) {
        var dssDocument = new InMemoryDocument(data.getDocument().getContent());
        var dssParams = SignatureParameterMapper.mapPAdESParameters(data.getSignatureParameters());
        dssParams.setSigningCertificate(activeCertificate.getDssPrivateKey().getCertificate());
        dssParams.setCertificateChain(activeCertificate.getDssPrivateKey().getCertificateChain());

        var service = new PAdESService(new CommonCertificateVerifier());
        var dataToSign = service.getDataToSign(dssDocument, dssParams);
        var token = getActiveCertificate().getToken().getDssToken();
        var signatureValue = token.sign(dataToSign, dssParams.getDigestAlgorithm(), activeCertificate.getDssPrivateKey());

        return service.signDocument(dssDocument, dssParams, signatureValue);
    }

    private DSSDocument signXAdES(SignatureUnit data) {
        var dssDocument = new InMemoryDocument(data.getDocument().getContent());
        var dssParams = SignatureParameterMapper.mapXAdESParameters(data.getSignatureParameters());
        dssParams.setSigningCertificate(activeCertificate.getDssPrivateKey().getCertificate());
        dssParams.setCertificateChain(activeCertificate.getDssPrivateKey().getCertificateChain());

        var service = new XAdESService(new CommonCertificateVerifier());
        var dataToSign = service.getDataToSign(dssDocument, dssParams);
        var token = getActiveCertificate().getToken().getDssToken();
        var signatureValue = token.sign(dataToSign, dssParams.getDigestAlgorithm(), activeCertificate.getDssPrivateKey());

        return service.signDocument(dssDocument, dssParams, signatureValue);
    }

    private DSSDocument signCAdES(SignatureUnit data) {
        var params = data.getSignatureParameters();

        var dssDocument = new InMemoryDocument(data.getDocument().getContent());
        if (isPresent(params.getContainerFilename()))
            dssDocument.setName(params.getContainerFilename());
        if (params.getFileMimeType() != null)
            dssDocument.setMimeType(params.getFileMimeType().toDSSMimeType());

        var dssParams = SignatureParameterMapper.mapASiCWithCAdESParameters(data.getSignatureParameters());
        dssParams.setSigningCertificate(activeCertificate.getDssPrivateKey().getCertificate());
        dssParams.setCertificateChain(activeCertificate.getDssPrivateKey().getCertificateChain());

        var service = new ASiCWithCAdESService(new CommonCertificateVerifier());
        var dataToSign = service.getDataToSign(dssDocument, dssParams);
        var token = getActiveCertificate().getToken().getDssToken();
        var signatureValue = token.sign(dataToSign, dssParams.getDigestAlgorithm(), activeCertificate.getDssPrivateKey());

        return service.signDocument(dssDocument, dssParams, signatureValue);
    }
}
