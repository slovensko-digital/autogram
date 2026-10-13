package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.AutogramMimeType;
import digital.slovensko.autogram.core.dto.SigningInput;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.util.List;

import static digital.slovensko.autogram.core.dto.AutogramMimeType.fromMimeTypeString;

final class TestSigningJobFactory {
    private TestSigningJobFactory() { }

    static SigningJob create(Batch batch, Responder responder) {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.txt",
                fromMimeTypeString("text/plain")), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES,
                DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false,
                null, null, null, false, 640, true);
        var prepared = SigningInput.prepareForASiCWithXAdES(document, parameters);
        var input = SigningInput.of(List.of(document), prepared.getParameters());
        return SigningJob.fromInput(input, responder, batch);
    }
}
