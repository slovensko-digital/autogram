package digital.slovensko.autogram.core.errors;

public class PINLockedException extends AutogramException {
    public PINLockedException() {
        super("Nastala chyba", "PIN je zablokovaný", "Pravdepodobne ste niekoľkokrát zadali nesprávny bezpečnostný kód, čo spôsobilo zablokovanie úložiska certifikátov.\n\nÚložisko sa väčšinou dá odblokovať pomocou obslužného softvéru od dodávateľa. Pokúste sa úložisko odblokovať a skúste znova.", null);
    }
}
