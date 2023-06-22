package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.IgnorableException;

public class FailedVisualizationException extends IgnorableException {

    public FailedVisualizationException(Throwable e, SigningJob job, Runnable onContinueCallback) {
        super(
            "Pri zobrazovaní dokumentu nastala chyba",
            "Chcete pokračovať v podpisovaní?",
            "Pri zobrazovaní dokumentu nastala neočakávaná chyba. Dokument je možné podpísať, ale uistite sa, že dôverujete zdroju dokumentu.\n\nKontaktujte správcu systému a nahláste mu chybu.",
            e, job, onContinueCallback);
    }
}
