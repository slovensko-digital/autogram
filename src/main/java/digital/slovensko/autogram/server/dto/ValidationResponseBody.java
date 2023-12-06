package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESContainerExtractor;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESContainerExtractor;
import eu.europa.esig.dss.diagnostic.SignerDataWrapper;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.reports.Reports;

import javax.security.auth.x500.X500Principal;
import java.util.List;

public record ValidationResponseBody(String formatSuboru, List<Podpis> podpisy, List<PodpisanyObjekt> podpisaneObjekty) {

    public static ValidationResponseBody build(Reports reports, DSSDocument document) throws DocumentNotSignedYetException {
        var sr = reports.getSimpleReport();
        var dd = reports.getDiagnosticData();
        var dr = reports.getDetailedReport();

        if (sr.getSignatureIdList().isEmpty())
            throw new DocumentNotSignedYetException();

        var f = sr.getContainerType();
        var formatSuboru = f == null ? sr.getSignatureFormat(sr.getSignatureIdList().get(0)).getSignatureForm().name() : f.name();

        List<Podpis> podpisy = dr.getSignatures().stream().map((e) -> {
            var conclusion = e.getConclusion();
            var timestamps = e.getTimestamps();
            var signingCertificate = dd.getCertificateById(dd.getSigningCertificateId(e.getId()));

            return new Podpis(
                    new VysledokOvereniaPodpisu(
                            conclusion.getIndication().ordinal(),
                            conclusion.getIndication().name()
                    ),
                    new InformaciaOPodpise(
                            sr.getSigningTime(e.getId()).toString(),
                            timestamps.isEmpty() ? null : sr.getProductionTime(timestamps.get(0).getId()).toString(),
                            sr.getSignatureFormat(e.getId()).name(),
                            new Podpisovycertifikat(
                                    new X500Principal(signingCertificate.getCertificateIssuerDN()).getName(X500Principal.RFC1779),
                                    new X500Principal(signingCertificate.getCertificateDN()).getName(X500Principal.RFC1779),
                                    signingCertificate.getSerialNumber(),
                                    signingCertificate.getNotBefore().toString(),
                                    signingCertificate.getNotAfter().toString(),
                                    new Typ(
                                            sr.getSignatureQualification(e.getId()).ordinal(),
                                            sr.getSignatureQualification(e.getId()).getReadable()
                                    )
                            ),
                            !timestamps.isEmpty(),
                            timestamps.isEmpty() ? null : timestamps.stream().map((t) -> {
                                var tsCertificate = dd.getCertificateById(dd.getTimestampSigningCertificateId(t.getId()));
                                return new CertifikatCasovejPeciatky(
                                    new X500Principal(tsCertificate.getCertificateIssuerDN()).getName(X500Principal.RFC1779),
                                    new X500Principal(tsCertificate.getCertificateDN()).getName(X500Principal.RFC1779),
                                    tsCertificate.getSerialNumber(),
                                    tsCertificate.getNotBefore().toString(),
                                    tsCertificate.getNotAfter().toString(),
                                    new Typ(
                                            sr.getTimestampQualification(t.getId()).ordinal(),
                                            sr.getTimestampQualification(t.getId()).getReadable()
                                    )

                            );}).toList(),
                            dd.getSignerDocuments(e.getId()).stream().map(SignerDataWrapper::getId).toList()
                    ));
        }).toList();

        List<PodpisanyObjekt> podpisaneObjekty = List.of();

        switch (sr.getSignatureFormat(sr.getSignatureIdList().get(0)).getSignatureForm()) {
            case PAdES: {
                podpisaneObjekty = dd.getAllSignerDocuments().stream().map((d) -> new PodpisanyObjekt(
                        d.getId(),
                        MimeTypeEnum.PDF.getMimeTypeString(),
                        d.getReferencedName()
                )).toList();
                break;
            }
            case XAdES: {
                var extractor = new ASiCWithXAdESContainerExtractor(document);
                var docs = extractor.extract().getSignedDocuments();

                podpisaneObjekty = dd.getAllSignerDocuments().stream().map((d) -> {
                    var r = docs.stream().filter((i) -> i.getName().equals(d.getReferencedName())).toList();
                    if (r.size() != 1)
                        return null;
                    var doc = r.get(0);
                    return new PodpisanyObjekt(
                            d.getId(),
                            doc.getMimeType().getMimeTypeString(),
                            d.getReferencedName()
                    );
                }).toList();
                break;
            }
            case CAdES: {
                var extractor = new ASiCWithCAdESContainerExtractor(document);
                var docs = extractor.extract().getSignedDocuments();

                podpisaneObjekty = dd.getAllSignerDocuments().stream().map((d) -> {
                    var r = docs.stream().filter((i) -> i.getName().equals(d.getReferencedName())).toList();
                    if (r.size() != 1)
                        return null;
                    var doc = r.get(0);
                    return new PodpisanyObjekt(
                            d.getId(),
                            doc.getMimeType().getMimeTypeString(),
                            d.getReferencedName()
                    );
                }).toList();
                break;
            }
            default:
        }

        return new ValidationResponseBody(formatSuboru, podpisy, podpisaneObjekty);
    }
}

record Podpis(VysledokOvereniaPodpisu vysledokOvereniaPodpisu, InformaciaOPodpise informaciaOPodpise) {
}

record VysledokOvereniaPodpisu(int kod, String popis) {
}

record InformaciaOPodpise(String datumACasPodpisuUtc, String datumACasCasovejPeciatkyPodpisuUtc, String typ,
                          Podpisovycertifikat podpisovycertifikat, boolean obsahujeCasovuPeciatku,
                          List<CertifikatCasovejPeciatky> certifikatyCasovejPeciatky, List<String> podpisaneObjekty) {
}

record Podpisovycertifikat(String vydavatel, String subjekt, String serioveCislo, String platnostOd, String platnostDo, Typ typ) {
}

record Typ(int kod, String popis) {
}

record CertifikatCasovejPeciatky(String vydavatel, String subjekt, String serioveCislo, String platnostOd, String platnostDo, Typ typ) {
}

record PodpisanyObjekt(String id, String mimeType, String nazov) {
}
