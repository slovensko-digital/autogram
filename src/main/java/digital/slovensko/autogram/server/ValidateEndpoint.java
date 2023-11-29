package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.ValidateRequestBody;
import digital.slovensko.autogram.server.dto.ValidateResponse;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.io.IOException;
import java.util.Base64;

public class ValidateEndpoint implements HttpHandler {
    private final Autogram autogram;

    public ValidateEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) {
        DSSDocument document = null;
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, ValidateRequestBody.class);
            document = new InMemoryDocument(Base64.getDecoder().decode(body.dataB64()));
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

            var html = SignatureValidator.getSignatureValidationReportHTML(reports);

            try {
                exchange.getResponseHeaders().add("Content-Type", "txt/xml;utf-8");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write(reports.getXmlSimpleReport().getBytes());
                exchange.getResponseBody().close();
            } catch (IOException e) {
                throw new ResponseNetworkErrorException("Externá aplikácia nečakala na odpoveď", e);
            }

//            EndpointUtils.respondWith(new ValidateResponse(reports.getXmlSimpleReport()), exchange);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
