package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.SignatureRequirementsNotMetException;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.model.DSSDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static digital.slovensko.autogram.util.DSSUtils.createDocumentValidator;

/** The selected goal, checked against the newly created signature before it is saved or delivered. */
public record SignatureRequirements(SignatureQualification qualification, boolean timestampRequested) {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureRequirements.class);

    public static SignatureRequirements fromCertificate(CertificateQualification qualification, boolean timestampRequested) {
        var expected = qualification == null ? null : switch (qualification) {
            case QCERT_FOR_ESIG_QSCD -> SignatureQualification.QESIG;
            case QCERT_FOR_ESIG -> SignatureQualification.ADESIG_QC;
            case QCERT_FOR_ESEAL_QSCD -> SignatureQualification.QESEAL;
            case QCERT_FOR_ESEAL -> SignatureQualification.ADESEAL_QC;
            default -> null;
        };
        return new SignatureRequirements(expected, timestampRequested);
    }

    public static Set<String> signatureIds(DSSDocument document) {
        var validator = createDocumentValidator(document);
        if (validator == null)
            return Set.of();
        // ASiC analyzers also require a verifier when only reading signature identifiers. No validation/network I/O.
        validator.setCertificateVerifier(NetworkClients.certificateVerifier());
        return validator.getSignatures().stream()
                .map(signature -> signature.getId()).collect(Collectors.toSet());
    }

    public void verify(DSSDocument document, Set<String> originalSignatureIds) {
        try {
            var validator = createDocumentValidator(document);
            if (validator == null)
                throw new SignatureRequirementsNotMetException("The signed document cannot be validated");
            var reports = SignatureValidator.getInstance().validate(validator);
            var simple = reports.getSimpleReport();
            var added = simple.getSignatureIdList().stream().filter(id -> !originalSignatureIds.contains(id)).toList();
            if (added.size() != 1)
                throw new SignatureRequirementsNotMetException("Expected one new signature, found " + added.size());

            var id = added.getFirst();
            // An unavailable certificate qualification has no promised level. Keep the existing ability to sign
            // with such certificates, but never accept a weaker result than a known, selected qualification.
            var actualQualification = reports.getDetailedReport().getSignatureQualification(id);
            if (qualification != null && (!simple.isValid(id) || actualQualification != qualification))
                throw new SignatureRequirementsNotMetException("Requested signature qualification: " + qualification
                        + "; actual: " + actualQualification + "; validation: " + simple.getIndication(id)
                        + "; subindication: " + simple.getSubIndication(id));

            var timestamps = simple.getSignatureTimestamps(id);
            if (timestampRequested != !timestamps.isEmpty())
                throw new SignatureRequirementsNotMetException("Timestamp requested: " + timestampRequested
                        + "; signature timestamps found: " + timestamps.size());
            for (var timestamp : timestamps) {
                if (timestamp.getIndication() != Indication.PASSED
                        || simple.getTimestampQualification(timestamp.getId()) != TimestampQualification.QTSA)
                    throw new SignatureRequirementsNotMetException("Requested timestamp qualification: QTSA; actual: "
                            + simple.getTimestampQualification(timestamp.getId()) + "; validation: " + timestamp.getIndication());
            }
        } catch (SignatureRequirementsNotMetException e) {
            LOGGER.warn("The new signature did not meet the selected goal", e);
            throw e;
        } catch (RuntimeException e) {
            LOGGER.warn("Could not validate the new signature against the selected goal", e);
            throw new SignatureRequirementsNotMetException(e);
        }
    }
}
