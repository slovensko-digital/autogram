package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.ValidationRequestBody;
import digital.slovensko.autogram.server.dto.ValidationResponseBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.io.IOException;
import java.util.Base64;

public class ValidationEndpoint implements HttpHandler {
    private final Autogram autogram;

    public ValidationEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) {
        DSSDocument document = null;
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, ValidationRequestBody.class);
            if (body.dataB64() == null)
                throw new IllegalArgumentException("Document to validate is not provided.");

            document = new InMemoryDocument(Base64.getDecoder().decode(body.dataB64()));
            if (document == null || document.openStream().readAllBytes().length < 1)
                throw new IllegalArgumentException("Document to validate is null.");
        } catch (JsonSyntaxException | IOException | IllegalArgumentException e) {
            var response = ErrorResponse.buildFromException(new MalformedBodyException(e.getMessage(), e));
            EndpointUtils.respondWithError(response, exchange);
        }

        try {
            var reports = SignatureValidator.getInstance().validate(document);
            if (reports == null) {
                EndpointUtils.respondWithError(ErrorResponse.buildFromException(new DocumentNotSignedYetException()), exchange);
                return;
            }

            try {
                EndpointUtils.respondWith(ValidationResponseBody.build(reports, document), exchange);
            } catch (Exception e) {
                EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
