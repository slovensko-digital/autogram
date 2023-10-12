package digital.slovensko.autogram.core.errors;

public class NoValidKeysDetectedException extends AutogramException {
    public NoValidKeysDetectedException() {
        super("Nastala chyba", "Nenašli sa žiadne platné podpisové klúče", "Na karte sa pravdepodobne nenachádzajú žiadne platné klúče, ktoré by sa dali použiť na podpisovanie. Boli však nájdené ekspirované kľúče, ktorými je možné podpisovať až po zmene v nastaveniach.\n\nV prípade nového občianskeho preukazu to môže znamenať, že si potrebujete certifikáty na podpisovanie cez občiansky preukaz vydať. Robí sa to pomocou eID klienta.", null);
    }
}
