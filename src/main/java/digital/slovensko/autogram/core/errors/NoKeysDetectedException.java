package digital.slovensko.autogram.core.errors;

public class NoKeysDetectedException extends AutogramException {
    public NoKeysDetectedException() {
        super("Nastala chyba", "Nenašli sa žiadne podpisové certifikáty", "V úložisku certifikátov sa pravdepodobne nenachádzajú žiadne platné podpisové certifikáty, ktoré by sa dali použiť na podpisovanie.\n\nV prípade nového občianskeho preukazu to môže znamenať, že si potrebujete certifikáty na podpisovanie cez občiansky preukaz vydať. Robí sa to pomocou obslužného softvéru eID klient.", null);
    }
}
