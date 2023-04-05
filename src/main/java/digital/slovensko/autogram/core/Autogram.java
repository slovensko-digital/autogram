package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;

import java.io.IOException;

public class Autogram {
    private final UI ui;

    private SigningKey activeKey;

    public Autogram(UI ui) {
        this.ui = ui;
    }

    public UI getUI() {
        return this.ui;
    }

    public void showSigningDialog(SigningJob job) {
        ui.showSigningDialog(job, this);
    }

    public void sign(SigningJob job) {
        var signedDocument = signDocument(job, activeKey);
        // TODO error handling
        job.onDocumentSigned(signedDocument);
        ui.hideSigningDialog(job, this);
    }

    public void start(String[] args) {
        ui.start(this, args);
    }

    public void pickSigningKey() {
        var drivers = TokenDriver.getAvailableDrivers(); // TODO handle empty driver list with ui.showError?
        ui.pickTokenDriverAndDo(drivers, (driver) -> {
            try {
                var token = driver.createToken();
                var keys = token.getKeys();
                ui.pickKeyAndDo(keys, (privateKey) -> {
                    setActiveSigningKey(new SigningKey(token, privateKey));
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DSSException e) {
                resetSigningKey();
                ui.showError(AutogramException.createFromDSSException(e));
            }
        });
    }

    private void setActiveSigningKey(SigningKey newKey) {
        activeKey = newKey;
        ui.refreshSigningKey();
    }

    public SigningKey getActiveSigningKey() {
        return activeKey;
    }

    public void resetSigningKey() {
        setActiveSigningKey(null);
    }

    private SignedDocument signDocument(SigningJob job, SigningKey key) {
        switch (job.getParameters().getSignatureType()) {
            case ASIC_XADES:
                return new SignedDocument(signDocumentAsAsiCWithXAdeS(job, key), key.getCertificate());
            case XADES:
                return new SignedDocument(signDocumentAsXAdeS(job, key), key.getCertificate());
            case ASIC_CADES:
                return new SignedDocument(signDocumentAsASiCWithCAdeS(job, key), key.getCertificate());
            case PADES:
                return new SignedDocument(signDocumentAsPAdeS(job, key), key.getCertificate());
            default:
                throw new RuntimeException("Unsupported signature type: " + job.getParameters().getSignatureType());
        }
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningJob job, SigningKey key) {
        DSSDocument doc = job.getDocument();
        if (job.getParameters().shouldCreateDatacontainer()) {
            var transformer = XDCTransformer.newInstance(job.getParameters());
            doc = transformer.transform(job.getDocument(), XDCTransformer.Mode.IDEMPOTENT);
        }

        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithXAdESService(commonCertificateVerifier);
        var signatureParameters = job.getParameters().getASiCWithXAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(doc, signatureParameters);
        var signatureValue = key.sign(dataToSign, job.getParameters().getDigestAlgorithm());

        return service.signDocument(doc, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsXAdeS(SigningJob job, SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new XAdESService(commonCertificateVerifier);
        var jobParameters = job.getParameters();
        var signatureParameters = job.getParameters().getXAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(job.getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(job.getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsASiCWithCAdeS(SigningJob job, SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithCAdESService(commonCertificateVerifier);
        var jobParameters = job.getParameters();
        var signatureParameters = job.getParameters().getASiCWithCAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(job.getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(job.getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsPAdeS(SigningJob job, SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new PAdESService(commonCertificateVerifier);
        var jobParameters = job.getParameters();
        var signatureParameters = job.getParameters().getPAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(job.getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(job.getDocument(), signatureParameters, signatureValue);
    }
}