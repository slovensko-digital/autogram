package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.TransformationException;
import eu.europa.esig.dss.enumerations.MimeType;

import java.io.IOException;

import org.xml.sax.SAXException;

public class SignEndpoint implements HttpHandler {
    private final Autogram autogram;

    public SignEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Allow preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, SignRequestBody.class);
            MimeType transformationOutputMimeTypeForXdc = null;
            if (body.getParameters().getContainer() != null) {
                transformationOutputMimeTypeForXdc = DocumentVisualizationBuilder
                        .getTransformationOutputMimeType(body.getParameters().getTransformation());
            }
            var job = new SigningJob(body.getDocument(), body.getParameters(), new ServerResponder(exchange),
                    transformationOutputMimeTypeForXdc);
            autogram.sign(job);
        } catch (JsonSyntaxException e) {
            var response = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(response, exchange);
        } catch (SAXException e) {
            System.out.println("SAXException: " + e.getMessage());
            EndpointUtils.respondWithError(
                    ErrorResponse.buildFromException(new TransformationException(e.getMessage(), e)),
                    exchange);
        } catch (Exception e) {
            e.printStackTrace();
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
