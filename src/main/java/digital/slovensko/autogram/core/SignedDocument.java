package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;

public class SignedDocument {
    private final DSSDocument dssDocument;
    private final CertificateToken certificate;

    public SignedDocument(DSSDocument dssDocument, CertificateToken certificate) {
        this.dssDocument = dssDocument;
        this.certificate = certificate;
    }

  public DSSDocument getDocument() {
    return dssDocument;
  }

  public CertificateToken getCertificate() {
    return certificate;
  }
}
