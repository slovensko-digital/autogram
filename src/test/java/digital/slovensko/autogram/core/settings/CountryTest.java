package digital.slovensko.autogram.core.settings;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryTest {
    @Test
    void greeceIsFoundByItsIsoCodeAndByItsTrustedListCode() {
        // ETSI EN 319 412-2 wants the ISO code, but e.g. ADACOM's CA certificates in the Greek trusted list have C=EL
        assertEquals("EL", Country.withTrustedList("GR").orElseThrow().getShortname());
        assertEquals("EL", Country.withTrustedList("EL").orElseThrow().getShortname());
    }

    @Test
    void countriesWithoutTrustedListAreNotFound() {
        assertEquals(Optional.empty(), Country.withTrustedList("GB"));
        assertEquals(Optional.empty(), Country.withTrustedList("EU"));
    }
}
