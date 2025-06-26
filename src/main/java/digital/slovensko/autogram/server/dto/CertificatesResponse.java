package digital.slovensko.autogram.server.dto;

import java.security.cert.X509Certificate;
import java.util.List;

public record CertificatesResponse(List<CertificateResponse> certificates) {
    public static CertificatesResponse buildFromList(List<X509Certificate> certificates) {
         return new CertificatesResponse(certificates.stream()
                .map(cert -> new CertificateResponse(cert.getSubjectX500Principal().toString(), cert.getIssuerX500Principal().toString()))
                .toList());
    }
}
