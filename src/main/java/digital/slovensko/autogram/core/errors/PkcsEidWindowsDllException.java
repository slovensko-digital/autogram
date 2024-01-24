package digital.slovensko.autogram.core.errors;

public class PkcsEidWindowsDllException extends AutogramException {
    public PkcsEidWindowsDllException(Exception e) {
        super("Chyba komunikácie s kartou", "Ovládač nie je možné použiť", "Nie je možné použiť ovládač pre podpisovanie vybranou kartou. Pravdepodobne je potrebné do systému nainštalovať balík Microsoft Visual C++ 2015 Redistributable.\n\nAk to nepomôže, kontaktujte nás na podpora@slovensko.digital", e);
    }
}
