package digital.slovensko.autogram.core.errors;

public class TokenNotRecognizedException extends AutogramException {
    public TokenNotRecognizedException() {
        super("Nastala chyba", "Kartu sa nepodarilo rozpoznať", "Pravdepodobne ste vybrali nespráve úložisko certifikátov a karta s ním nie je kompatibilná alebo je nastavený neplatný slot karty.\n\nUistite sa, že máte nainštalovaný správny ovládač pre daný typ karty, skontrolujte nastavenia Autogramu a skúste znova.");
    }
}
