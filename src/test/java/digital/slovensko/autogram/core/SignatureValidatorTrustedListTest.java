package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.job.ValidationInfoRecord;
import eu.europa.esig.dss.model.tsl.LOTLInfo;
import eu.europa.esig.dss.model.tsl.TLInfo;
import eu.europa.esig.dss.model.tsl.TLParsingInfoRecord;
import eu.europa.esig.dss.model.tsl.TLValidationJobSummary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignatureValidatorTrustedListTest {
    @Test
    void noLoadedListsAreNotConsideredValid() {
        var status = SignatureValidator.getTrustedListStatus(List.of("SK"), null);

        assertFalse(status.isComplete());
        assertEquals(Set.of(), status.validatedCountries());
    }

    @Test
    void processedButInvalidListIsNotConsideredValid() {
        var status = SignatureValidator.getTrustedListStatus(List.of("SK"), summary(true, country("SK", false)));

        assertFalse(status.isComplete());
        assertEquals(Set.of(), status.validatedCountries());
    }

    @Test
    void allSelectedListsMustBeValidated() {
        var status = SignatureValidator.getTrustedListStatus(List.of("SK", "CZ"), summary(true, country("SK", true)));

        assertFalse(status.isComplete());
        assertEquals(Set.of("SK"), status.validatedCountries());
    }

    @Test
    void listWithoutValidatedLotlIsNotTrusted() {
        var status = SignatureValidator.getTrustedListStatus(List.of("SK"), summary(false, country("SK", true)));

        assertFalse(status.isComplete());
    }

    @Test
    void allSelectedListsAndLotlValidated() {
        var status = SignatureValidator.getTrustedListStatus(List.of("SK", "CZ"),
                summary(true, country("SK", true), country("CZ", true)));

        assertTrue(status.isComplete());
        assertEquals(Set.of("SK", "CZ"), status.validatedCountries());
    }

    private static TLInfo country(String code, boolean valid) {
        var info = mock(TLInfo.class);
        var parsing = mock(TLParsingInfoRecord.class);
        var validation = mock(ValidationInfoRecord.class);
        when(parsing.getTerritory()).thenReturn(code);
        when(validation.isValid()).thenReturn(valid);
        when(info.getParsingCacheInfo()).thenReturn(parsing);
        when(info.getValidationCacheInfo()).thenReturn(validation);
        return info;
    }

    private static TLValidationJobSummary summary(boolean lotlValid, TLInfo... countries) {
        var summary = mock(TLValidationJobSummary.class);
        var lotl = mock(LOTLInfo.class);
        var validation = mock(ValidationInfoRecord.class);
        when(validation.isValid()).thenReturn(lotlValid);
        when(lotl.getValidationCacheInfo()).thenReturn(validation);
        when(lotl.getTLInfos()).thenReturn(List.of(countries));
        when(summary.getLOTLInfos()).thenReturn(List.of(lotl));
        return summary;
    }
}
