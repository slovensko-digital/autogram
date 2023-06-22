package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.IgnorableException;

public class FailedVisualizationException extends IgnorableException {

    public FailedVisualizationException(Throwable e, SigningJob job, Runnable onContinueCallback) {
        super(
            "Pri zobrazovaní dokumentu nastala chyba.",
            "Chcete pokračovať v podpisovaní?", 
                "Stále je možné dokument podpísať, ale uistite sa, že dôverujete zdroju dokumentu.\nKontaktujte správcu systému a nahláste mu chybu.",
                e, job, onContinueCallback);
    }
}
