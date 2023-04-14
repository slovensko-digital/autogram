package digital.slovensko.autogram;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.ui.SaveFileResponder;
import digital.slovensko.autogram.ui.cli.CliUI;
import digital.slovensko.autogram.ui.gui.GUI;
import eu.europa.esig.dss.model.FileDocument;

import java.io.File;
import java.util.Arrays;

public class Main {
    public static void main2(String[] args) {
        var ui = new CliUI();

        var autogram = new Autogram(ui);

        var document = new FileDocument("pom.xml");
        var parameters = new SigningParameters();
        var responder = new SaveFileResponder(new File("dummy.pdf"));

        autogram.pickSigningKey();
        autogram.showSigningDialog(new SigningJob(document, parameters, responder));

        // sign another without picking cert again and no BOK entered
        document = new FileDocument("pom.xml");

        autogram.showSigningDialog(new SigningJob(document, parameters, responder));
    }

    public static void main(String[] args) {
        System.out.println("Starting with args: " + Arrays.toString(args));
        var ui = new GUI();
        var autogram = new Autogram(ui);

        autogram.start(args);
    }
}
