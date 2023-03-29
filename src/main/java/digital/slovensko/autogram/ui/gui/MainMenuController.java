package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.model.FileDocument;

public class MainMenuController {
    private GUI ui;
    private Autogram autogram;

    public MainMenuController(GUI ui, Autogram autogram) {
        this.ui = ui;
        this.autogram = autogram;
    }

    public void onTestButtonAction() {
        var document = new FileDocument("test2.pdf");
        var parameters = new SigningParameters();
        var responder = new GUIResponder(ui);

        var job = new SigningJob(document, parameters, responder);
        autogram.showSigningDialog(job);
    }
}
