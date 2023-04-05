package digital.slovensko.autogram.core.errors;

public class TokenNotRecognizedException extends AutogramException {
    public TokenNotRecognizedException() {
        super("Nastala chyba", "Kartu sa nepodarilo rozpoznať", "Pravdepodobne ste vybrali nespráve úložisko certifikátov a karta s ním nie je kompatibilná.\n\nUistite sa, že máte nainštalovaný správny ovládač pre daný typ karty a skúste znova.");
    }
}
