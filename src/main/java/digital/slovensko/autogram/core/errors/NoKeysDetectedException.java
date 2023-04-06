package digital.slovensko.autogram.core.errors;

public class NoKeysDetectedException extends AutogramException {
    public NoKeysDetectedException() {
        super("Nastala chyba", "Nenašli sa žiadne podpisové klúče", "Na karte sa pravdepodobne nenachádzajú žiadne klúče, ktoré by sa dali použiť na podpisovanie.\n\nV prípade nového občianskeho preukazu to môže znamenať, že si potrebujete certifikáty na podpisovanie cez občiansky preukaz vydať. Robí sa to pomocou eID klienta.", null);
    }
}
