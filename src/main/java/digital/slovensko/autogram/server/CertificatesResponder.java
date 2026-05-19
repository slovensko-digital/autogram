package digital.slovensko.autogram.server;


import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.server.dto.CertificatesResponse;
import digital.slovensko.autogram.server.dto.ErrorResponse;

import java.security.cert.X509Certificate;
import java.util.List;

public class CertificatesResponder {
    private final HttpExchange exchange;

    public CertificatesResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public void onSuccess(List<X509Certificate> certificates) {
        EndpointUtils.respondWith(CertificatesResponse.buildFromList(certificates), exchange);
    }

    public void onError(Exception error) {
        EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(error), exchange);
    }
}
