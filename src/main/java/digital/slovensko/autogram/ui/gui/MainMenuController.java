package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.model.FileDocument;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.awt.*;

public class MainMenuController {
    private GUI ui;
    private Autogram autogram;

    public void initialize(GUI ui, Autogram autogram) {
        this.ui = ui;
        this.autogram = autogram;
    }

    public void onTestButtonAction(ActionEvent event) {
        var document = new FileDocument("pom.xml");
        var parameters = new SigningParameters();
        var responder = new GUIResponder(ui);

        var job = new SigningJob(document, parameters, responder);
        autogram.showSigningDialog(job);
    }
}
