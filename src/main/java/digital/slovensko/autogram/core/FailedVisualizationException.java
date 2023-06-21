package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.IgnorableException;

public class FailedVisualizationException extends IgnorableException {

    public FailedVisualizationException(Throwable e, SigningJob job, Runnable onContinueCallback) {
        super("Neočakávaná chyba", "Nastala neznáma chyba", "Pri zobrazovaní dokumentu nastala neočakávaná chyba.", e, job, onContinueCallback);
    }
}
