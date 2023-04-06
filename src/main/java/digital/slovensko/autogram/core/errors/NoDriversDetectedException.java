package digital.slovensko.autogram.core.errors;

public class NoDriversDetectedException extends AutogramException {

    public NoDriversDetectedException() {
        super("Nastala chyba", "Nenašli sa žiadne známe ovládače", "Ak používate občiansky preukaz, tak je potrebné mať nainštalováný eID klient. Inštalačné balíky nájdete na www.slovensko.sk\n\nV prípade použitia iných kariet je potrebné mať nainštalovaný obslužný ovládač od ich výrobcu.\n\nNainštalujte obslužný softvér a skúste znova.", null);
    }
}
