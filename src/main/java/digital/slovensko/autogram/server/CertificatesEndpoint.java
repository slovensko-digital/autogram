package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.server.dto.ErrorResponse;

import java.util.List;

public class CertificatesEndpoint implements HttpHandler {
    private final Autogram autogram;

    public CertificatesEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            List<String> drivers = EndpointUtils.parseQueryParam(query, "driver");

            var responder = new CertificatesResponder(exchange);
            autogram.consentCertificateReadingAndThen(responder, drivers);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(e), exchange);
        }
    }
}
