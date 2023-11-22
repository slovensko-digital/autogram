package digital.slovensko.autogram.core.errors;

public class KeyPinDifferentFromTokenPin extends AutogramException {
    public KeyPinDifferentFromTokenPin(Throwable e) {
        super("Nepodporovaný PIN certifikátu", "PIN podpisového certifikátu je iný než PIN úložiska certifikátov. Ďalšie podpisovanie môže viesť k zablokovaniu karty!", "Použitý certifikát má nastavený atribút CKA_ALWAYS_AUTHENTICATE a jeho PIN je iný než PIN úložiska certifikátov. Toto zatiaľ nepodporujeme. Kontaktujte nás, prosím, na podpora@slovensko.digital", e);
    }
}
