package digital.slovensko.autogram.core.settings;

public class Country {

    private String name;

    private String shortname;

    public Country(String name, String shrotname) {
        this.name = name;
        this.shortname = shrotname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shrotname) {
        this.shortname = shrotname;
    }
}
