package digital.slovensko.autogram.core.errors;

public class TokenRemovedException extends AutogramException {
    public TokenRemovedException() {
        super("Nastala chyba", "Karta bola vytiahnutá", "Pravdepodobne ste vytiahli kartu z čítačky. Zasuňte ju naspäť a skúste znova.");
    }
}
