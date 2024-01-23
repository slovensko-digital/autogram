package digital.slovensko.autogram.core.errors;

public class PkcsEidWindowsDllException extends AutogramException {
    public PkcsEidWindowsDllException(Exception e) {
        super("Nastala chyba", "eID ovládač nie je možné použiť", "Nie je možné použiť eID ovládač pre podpisovanie občianskym preukazom. Pravdepodobne je potrebné do systému nainštalovať balík Microsoft Visual C++ 2015 <a href=\"https://www.google.com\">Redistributable</a>.\n\nAk to nepomôže, kontaktujte nás na podpora@slovensko.digital", e);
    }
}
