package digital.slovensko.autogram.core;

import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.util.Objects;

/** A local signing choice, optionally shared by the documents of one bulk operation. */
public record SignatureChoice(SignatureRequirements requirements, TSPSource timestampSource) {
    public SignatureChoice {
        Objects.requireNonNull(requirements, "signature requirements");
        if (requirements.timestampRequested())
            Objects.requireNonNull(timestampSource, "timestamp source");
    }

    public void applyTo(SigningJob job) {
        job.setTimestampEnabled(requirements.timestampRequested(), timestampSource);
        job.setSignatureRequirements(requirements);
    }
}
