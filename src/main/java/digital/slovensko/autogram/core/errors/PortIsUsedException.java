package digital.slovensko.autogram.core.errors;

public class PortIsUsedException extends AutogramException {
    public PortIsUsedException() {
        super("Nepodarilo sa spustiť server", "Autogram je spustený bez možnosti podpisovania z prehliadača", "Port, ktorý používa Autogram server, je momentálne obsadený inou aplikáciou. Autogram sa preto spustil bez možnosti podpisovania z prehliadača. Overovanie a podpisovanie súborov v desktopovom režime je funkčné.\n\nAk chcete Autogram používať plnohodnotne, zatvorte danú aplikáciu a skúste spustiť Autogram znova.", null);
    }
}
