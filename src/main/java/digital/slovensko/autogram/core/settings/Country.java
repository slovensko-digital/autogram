package digital.slovensko.autogram.core.settings;

import java.util.Locale;

public class Country {

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

    public String getName(Locale inLocale) {
        if (inLocale == null) return isoCode;

        return new Locale(inLocale.getLanguage(), isoCode).getDisplayCountry(inLocale);
    }


    public String getShortname() {
        return shortname;
    }

}
