package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESContainerExtractor;
import eu.europa.esig.dss.asic.common.AbstractASiCContainerExtractor;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESContainerExtractor;
import eu.europa.esig.dss.diagnostic.SignerDataWrapper;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.reports.Reports;

import javax.security.auth.x500.X500Principal;
import java.text.SimpleDateFormat;
import java.util.List;

public record ValidationResponseBody(String fileFormat, List<Signature> signatures, List<SignedObject> signedObjects,
                                     List<UnsignedObject> unsignedObjects) {
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

                                return new TimestampCertificateInfo(
                                        new X500Principal(tsCertificate.getCertificateIssuerDN()).getName(X500Principal.RFC1779),
                                        new X500Principal(tsCertificate.getCertificateDN()).getName(X500Principal.RFC1779),
                                        tsCertificate.getSerialNumber(),
                                        format.format(sr.getProductionTime(t.getId())),
                                        format.format(tsCertificate.getNotBefore()),
                                        format.format(tsCertificate.getNotAfter()),
                                        new Result(
                                                sr.getTimestampQualification(t.getId()).ordinal(),
                                                sr.getTimestampQualification(t.getId()).getReadable()
                                        ),
                                        dd.getTimestampType(t.getId()).name()
                                );
                            }).toList(),
                            dd.getSignerDocuments(e.getId()).stream().map(SignerDataWrapper::getId).toList()
                    ));
        }).toList();

        List<SignedObject> signedObjects = null;
        List<UnsignedObject> unsignedObjects = null;
        AbstractASiCContainerExtractor extractor = null;

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
                extractor = new ASiCWithXAdESContainerExtractor(document);
                break;
            }
            case CAdES: {
                extractor = new ASiCWithCAdESContainerExtractor(document);
                break;
            }
            default:
        }

        if (extractor != null) {
            var allObjects = extractor.extract().getSignedDocuments();
            signedObjects = getSignedObjects(allObjects, dd.getAllSignerDocuments());
            unsignedObjects = getUnsignedObjects(allObjects, dd.getAllSignerDocuments());
        }

        return new ValidationResponseBody(fileFormat, signatures, signedObjects, unsignedObjects);
    }

    private static List<SignedObject> getSignedObjects(List<DSSDocument> docs, List<SignerDataWrapper> signedObjects) {
        return signedObjects.stream().map((signedObject) -> {
            var r = docs.stream().filter((doc) -> doc.getName().equals(signedObject.getReferencedName())).toList();
            if (r.isEmpty())
                return null;

            return new SignedObject(
                    signedObject.getId(),
                    r.get(0).getMimeType().getMimeTypeString(),
                    signedObject.getReferencedName()
            );
        }).toList();
    }

    private static List<UnsignedObject> getUnsignedObjects(List<DSSDocument> docs, List<SignerDataWrapper> signedObjects) {
        return docs.stream().filter((o) -> signedObjects.stream().filter((s) -> o.getName().equals(s.getReferencedName())).toList().isEmpty()).map((generalObject) ->
                new UnsignedObject(
                        generalObject.getMimeType().getMimeTypeString(),
                        generalObject.getName()
                )
        ).toList();
    }

    record Signature(Result validationResult, SignatureInfo signatureInfo) {
    }

    record Result(int code, String description) {
    }

    record SignatureInfo(String level, String claimedSigningTime, boolean isTimestamped, String timestampSigningTime,
                         CertificateInfo signingCertificate, List<TimestampCertificateInfo> timestamps,
                         List<String> signedObjectsIds) {
    }

    record CertificateInfo(String issuerDN, String subjectDN, String serialNumber, String productionTime,
                           String notBefore,
                           String notAfter, Result qualification) {
    }

    record TimestampCertificateInfo(String issuerDN, String subjectDN, String serialNumber, String productionTime,
                                    String notBefore,
                                    String notAfter, Result qualification, String timestampType) {
    }

    record SignedObject(String id, String mimeType, String filename) {
    }

    record UnsignedObject(String mimeType, String filename) {
    }
}
