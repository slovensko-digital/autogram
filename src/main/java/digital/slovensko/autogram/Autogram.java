package digital.slovensko.autogram;

import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;

public class Autogram {

    private final UI ui;

    private SigningKey activeKey;

    public Autogram(UI ui) {
        this.ui = ui;
    }

    public void showSigningDialog(SigningJob job) {
        ui.showSigningDialog(job, this);
    }

    public void sign(SigningJob job) {
        withActiveSigningKeyDo((key) -> {
            var signedDocument = signDocument(job, key);
            // TODO error handling
            job.onDocumentSigned(signedDocument);
        });
    }

    private void withActiveSigningKeyDo(SigningKeyLambda signingKeyCallback) {
        if (activeKey != null) {
            signingKeyCallback.call(activeKey);
        } else {
            var drivers = TokenDriver.getAvailableDrivers(); // TODO move up?
            ui.pickTokenDriverAndDo(drivers, (driver) -> {
                try {
                    var token = driver.createToken();
                    var keys = token.getKeys();
                    ui.pickKeyAndDo(keys, (privateKey) -> {
                        setActiveSigningKey(new SigningKey(token, privateKey));
                        signingKeyCallback.call(activeKey);
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void setActiveSigningKey(SigningKey newKey) {
        activeKey = newKey;
        ui.refreshSigningKey(activeKey);
    }

    private DSSDocument signDocument(SigningJob job, SigningKey key) {
        return signDocumentAsAsiCWithXAdeS(job, key);
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningJob job, SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithXAdESService(commonCertificateVerifier);

        // TODO load from params actually
        var parameters = new ASiCWithXAdESSignatureParameters();
        parameters.aSiC().setContainerType(ASiCContainerType.ASiC_E);
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(key.getCertificate());
        parameters.setSigningCertificateDigestMethod(DigestAlgorithm.SHA256);
        parameters.setCertificateChain(key.getCertificateChain());
        parameters.setSignedInfoCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE);
        parameters.setSignedPropertiesCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE);
        parameters.setEn319132(false);

        var dataToSign = service.getDataToSign(job.getDocument(), parameters);
        var signatureValue = key.sign(dataToSign, DigestAlgorithm.SHA256);

        return service.signDocument(job.getDocument(), parameters, signatureValue);
    }
}
