package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESContainerExtractor;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESContainerExtractor;
import eu.europa.esig.dss.diagnostic.SignerDataWrapper;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.reports.Reports;

import javax.security.auth.x500.X500Principal;
import java.text.SimpleDateFormat;
import java.util.List;

public record ValidationResponseBody(String fileFormat, List<Signature> signatures, List<SignedObject> signedObjects) {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss Z");

    public static ValidationResponseBody build(Reports reports, DSSDocument document) throws DocumentNotSignedYetException {
        var sr = reports.getSimpleReport();
        var dd = reports.getDiagnosticData();
        var dr = reports.getDetailedReport();

        if (sr.getSignatureIdList().isEmpty())
            throw new DocumentNotSignedYetException();

        var f = sr.getContainerType();
        var fileFormat = f == null ? sr.getSignatureFormat(sr.getSignatureIdList().get(0)).getSignatureForm().name() : f.name();

        List<Signature> signatures = dr.getSignatures().stream().map((e) -> {
            var conclusion = e.getConclusion();
            var timestamps = e.getTimestamps();
            var signingCertificate = dd.getCertificateById(dd.getSigningCertificateId(e.getId()));

            return new Signature(
                new Result(
                    conclusion.getIndication().ordinal(),
                    conclusion.getIndication().name()
                ),
                new SignatureInfo(
                    sr.getSignatureFormat(e.getId()).name(),
                    format.format(sr.getSigningTime(e.getId())),
                    !timestamps.isEmpty(),
                    timestamps.isEmpty() ? null : format.format(sr.getProductionTime(timestamps.get(0).getId())),
                    new CertificateInfo(
                        new X500Principal(signingCertificate.getCertificateIssuerDN()).getName(X500Principal.RFC1779),
                        new X500Principal(signingCertificate.getCertificateDN()).getName(X500Principal.RFC1779),
                        signingCertificate.getSerialNumber(),
                        format.format(sr.getSigningTime(e.getId())),
                        format.format(signingCertificate.getNotBefore()),
                        format.format(signingCertificate.getNotAfter()),
                        new Result(
                            sr.getSignatureQualification(e.getId()).ordinal(),
                            sr.getSignatureQualification(e.getId()).getReadable()
                        )
                    ),
                    timestamps.isEmpty() ? null : timestamps.stream().map((t) -> {
                        var tsCertificate = dd.getCertificateById(dd.getTimestampSigningCertificateId(t.getId()));

                        return new CertificateInfo(
                            new X500Principal(tsCertificate.getCertificateIssuerDN()).getName(X500Principal.RFC1779),
                            new X500Principal(tsCertificate.getCertificateDN()).getName(X500Principal.RFC1779),
                            tsCertificate.getSerialNumber(),
                            format.format(sr.getProductionTime(t.getId())),
                            format.format(tsCertificate.getNotBefore()),
                            format.format(tsCertificate.getNotAfter()),
                            new Result(
                                sr.getTimestampQualification(t.getId()).ordinal(),
                                sr.getTimestampQualification(t.getId()).getReadable()
                            )
                        );
                    }).toList(),
                    dd.getSignerDocuments(e.getId()).stream().map(SignerDataWrapper::getId).toList()
                ));
        }).toList();

        List<SignedObject> signedObjects = List.of();

        switch (sr.getSignatureFormat(sr.getSignatureIdList().get(0)).getSignatureForm()) {
            case PAdES: {
                signedObjects = dd.getAllSignerDocuments().stream().map((d) -> new SignedObject(
                    d.getId(),
                    MimeTypeEnum.PDF.getMimeTypeString(),
                    d.getReferencedName()
                )).toList();
                break;
            }
            case XAdES: {
                var extractor = new ASiCWithXAdESContainerExtractor(document);
                var docs = extractor.extract().getSignedDocuments();

                signedObjects = dd.getAllSignerDocuments().stream().map((d) -> {
                    var r = docs.stream().filter((i) -> i.getName().equals(d.getReferencedName())).toList();
                    if (r.size() != 1)
                        return null;

                    var doc = r.get(0);
                    return new SignedObject(
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

                signedObjects = dd.getAllSignerDocuments().stream().map((d) -> {
                    var r = docs.stream().filter((i) -> i.getName().equals(d.getReferencedName())).toList();
                    if (r.size() != 1)
                        return null;

                    var doc = r.get(0);
                    return new SignedObject(
                        d.getId(),
                        doc.getMimeType().getMimeTypeString(),
                        d.getReferencedName()
                    );
                }).toList();
                break;
            }
            default:
        }

        return new ValidationResponseBody(fileFormat, signatures, signedObjects);
    }
}

record Signature(Result validaitonResult, SignatureInfo signatureInfo) {
}

record Result(int code, String description) {
}

record SignatureInfo(String level, String claimedSigningTime, boolean isTimestamped, String timestampSigningTime,
                     CertificateInfo signingCertificate, List<CertificateInfo> timestamps,
                     List<String> signedObjectsIds) {
}

record CertificateInfo(String issuerDN, String subjectDN, String serialNumber, String productionTime, String notBefore,
                       String notAfter, Result qualification) {
}

record SignedObject(String id, String mimeType, String filename) {
}
