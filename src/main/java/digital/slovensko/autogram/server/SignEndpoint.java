package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.XMLValidator;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;

import java.io.IOException;
import java.util.Base64;

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
            var job = new SigningJob(body.getDocument(), body.getParameters(), new ServerResponder(exchange));

            validateXML(body);

            autogram.sign(job);
        } catch (JsonSyntaxException e) {
            var response = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(response, exchange);
        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }

    private static void validateXML(SignRequestBody body) {
        String xmlContent = body.isBase64() ? new String(Base64.getDecoder().decode(body.getContent())) : body.getContent();
        String xsdScheme = body.getParameters().getSchema();

        if (xsdScheme == null) {
            return;
        }

        boolean result = new XMLValidator(xmlContent, xsdScheme).validate();
        if (!result) {
            throw new RequestValidationException("XML validation against XSD scheme failed", "");
        }
    }
}
