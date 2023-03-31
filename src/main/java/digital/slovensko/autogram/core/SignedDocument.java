package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.DSSDocument;

public class SignedDocument {
    private final DSSDocument dssDocument;
    private final SigningKey key;

    public SignedDocument(DSSDocument dssDocument, SigningKey key) {
        this.dssDocument = dssDocument;
        this.key = key;
    }

  public DSSDocument getDocument() {
    return dssDocument;
  }

  public SigningKey getKey() {
    return key;
  }
}
