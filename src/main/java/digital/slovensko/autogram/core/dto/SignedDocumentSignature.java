package digital.slovensko.autogram.core.dto;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

public record SignedDocumentSignature(SignatureForm form, ASiCContainerType container, SignaturePackaging packaging) {
}