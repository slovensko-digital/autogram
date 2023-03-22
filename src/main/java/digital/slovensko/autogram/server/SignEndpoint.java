package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.ui.cli.CliResponder;
import eu.europa.esig.dss.model.FileDocument;

import java.io.IOException;

public class SignEndpoint implements HttpHandler {
    private final Autogram autogram;

    public SignEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Received a HTTP sign request");

        // TODO replace with actual data
        var document = new FileDocument("pom.xml");

        var parameters = new SigningParameters();
        var responder = new ServerResponder(exchange);

        var job = new SigningJob(document, parameters, responder);

        autogram.showSigningDialog(job);
    }
}
