package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Explicitly opted-in network test; not part of the default offline build. */
@Tag("HttpSmokeTest")
class TrustedListLiveSmokeTest {
    @Test
    void validatesCurrentSlovakTrustedListThroughEuLotl() {
        var executor = Executors.newFixedThreadPool(4);
        try {
            var validator = SignatureValidator.getInstance();
            validator.initialize(executor, List.of("SK"));

            var status = validator.getTrustedListStatus();
            assertTrue(status.isComplete(), () -> "EU LOTL and SK TL must validate: " + status);
        } finally {
            executor.shutdownNow();
        }
    }
}
