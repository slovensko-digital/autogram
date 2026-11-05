package digital.slovensko.autogram.core.settings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Country {
    /** Countries whose trusted lists can be turned on in the settings. */
    public static final List<Country> TRUSTED_LIST_COUNTRIES = List.of(
            new Country("BE"),
            new Country("BG"),
            new Country("CZ"),
            new Country("HR"),
            new Country("CY"),
            new Country("DK"),
            new Country("EE"),
            new Country("FI"),
            new Country("FR"),
            new Country("EL", "GR"),
            new Country("NL"),
            new Country("IE"),
            new Country("LT"),
            new Country("LV"),
            new Country("LU"),
            new Country("HU"),
            new Country("MT"),
            new Country("DE"),
            new Country("PL"),
            new Country("PT"),
            new Country("AT"),
            new Country("RO"),
            new Country("SK"),
            new Country("SI"),
            new Country("ES"),
            new Country("SE"),
            new Country("IT"));

    private final String shortname;
    private final String isoCode;

    public Country(String shortname) {
        this.shortname = shortname;
        this.isoCode = shortname;
    }

    public Country(String shortname, String isoCode) {
        this.shortname = shortname;
        this.isoCode = isoCode;
    }

    /** By the ISO code, or the trusted list one, which some certificates use too, e.g. EL for Greece. */
    public static Optional<Country> withTrustedList(String code) {
        return TRUSTED_LIST_COUNTRIES.stream()
                .filter(country -> country.isoCode.equals(code) || country.shortname.equals(code))
                .findFirst();
    }

    public String getName(Locale inLocale) {
        if (inLocale == null) return isoCode;

        return new Locale(inLocale.getLanguage(), isoCode).getDisplayCountry(inLocale);
    }


    public String getShortname() {
        return shortname;
    }

}
