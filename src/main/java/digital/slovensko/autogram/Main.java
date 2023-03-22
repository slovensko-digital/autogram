package digital.slovensko.autogram;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.ui.cli.CliResponder;
import digital.slovensko.autogram.ui.cli.CliUI;
import digital.slovensko.autogram.ui.gui.GUI;
import digital.slovensko.autogram.ui.gui.GUIResponder;
import eu.europa.esig.dss.model.FileDocument;

public class Main {
    public static void main2(String[] args) {
        var ui = new CliUI();

        var autogram = new Autogram(ui);
        var document = new FileDocument("pom.xml");

        var parameters = new SigningParameters();
        var responder = new CliResponder();

        var job = new SigningJob(document, parameters, responder);

        autogram.showSigningDialog(job);

        // sign another without picking cert again and no BOK entered
        document = new FileDocument("pom.xml");
        job = new SigningJob(document, parameters, responder);

        autogram.showSigningDialog(job);
    }

    public static void main(String[] args) {
        var ui = new GUI();
        var autogram = new Autogram(ui);

        autogram.start(args);
    }
}
