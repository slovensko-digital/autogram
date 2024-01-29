package digital.slovensko.autogram.core.errors;

public class UnknownEformException  extends AutogramException {
    public UnknownEformException() {
        super("Neznámy formulár", "Formulár nie je možné podpísať", "Zvolený dokument nie je možné podpísať ako štátny elektronický formulár. Ak ste chceli podpísať elektronický formulár, ozvite sa nám, prosím, emailom na podpora@slovensko.digital");
    }
}
