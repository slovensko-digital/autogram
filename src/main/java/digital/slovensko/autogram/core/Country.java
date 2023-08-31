package digital.slovensko.autogram.core;

public class Country {

    private String name;

    private String shrotname;

    public Country(String name, String shrotname) {
        this.name = name;
        this.shrotname = shrotname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShrotname() {
        return shrotname;
    }

    public void setShrotname(String shrotname) {
        this.shrotname = shrotname;
    }
}
