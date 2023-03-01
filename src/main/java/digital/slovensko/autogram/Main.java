package digital.slovensko.autogram;

import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        var ui = new CliUI();

        var autogram = new Autogram(ui);
        var document = new FileDocument("pom.xml");

        var parameters = new SigningParameters();
        var responder = new CLIResponder();

        var job = new SigningJob(document, parameters, responder);

        autogram.showSigningDialog(job);

        // sign another without picking cert again and no BOK entered
        document = new FileDocument("pom.xml");
        job = new SigningJob(document, parameters, responder);

        autogram.showSigningDialog(job);
    }
}
