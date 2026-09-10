package digital.slovensko.autogram.core;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import eu.europa.esig.dss.diagnostic.jaxb.XmlSignatureScope;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.reports.Reports;

public class ValidationReports {
    public record SignatureEntry(DocumentReport documentReport, String signatureId) {}

    public record DocumentReport(int documentIndex, DSSDocument document, Reports reports) {
        public DocumentReport {
            Objects.requireNonNull(document, "document");
            Objects.requireNonNull(reports, "reports");
        }

        public boolean haveInvalidSignatures() {
            for (var signatureId : reports.getSimpleReport().getSignatureIdList()) {
                if (!reports.getSimpleReport().isValid(signatureId))
                    return true;
            }

            return false;
        }

        public int getSignatureCount() {
            return reports.getSimpleReport().getSignaturesCount();
        }

        public boolean hasMultipleContainerDocuments() {
            var containerInfo = reports.getDiagnosticData().getContainerInfo();
            return containerInfo != null
                    && containerInfo.getContentFiles() != null
                    && containerInfo.getContentFiles().size() > 1;
        }

        public List<String> getContainerContentFiles() {
            var containerInfo = reports.getDiagnosticData().getContainerInfo();
            if (containerInfo == null || containerInfo.getContentFiles() == null)
                return List.of();

            return List.copyOf(containerInfo.getContentFiles());
        }

        public List<String> getSignatureScopeDocumentNames(String signatureId) {
            var signature = reports.getDiagnosticData().getSignatureById(signatureId);
            if (signature == null || signature.getSignatureScopes() == null)
                return List.of();

            return signature.getSignatureScopes().stream()
                    .map(XmlSignatureScope::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .distinct()
                    .toList();
        }

        public boolean signatureCoversAllDocuments(String signatureId) {
            var contentFiles = getContainerContentFiles();
            if (contentFiles.isEmpty())
                return false;

            var coveredFiles = new LinkedHashSet<>(getSignatureScopeDocumentNames(signatureId));
            return coveredFiles.containsAll(contentFiles);
        }
    }

    private final List<DocumentReport> documentReports;
    private final SigningJob signingJob;

    public ValidationReports(List<DocumentReport> documentReports, SigningJob signingJob) {
        this.documentReports = List.copyOf(documentReports);
        this.signingJob = signingJob;
    }

    public Reports getReports() {
        return documentReports.isEmpty() ? null : documentReports.get(0).reports();
    }

    public List<DocumentReport> getDocumentReports() {
        return documentReports;
    }

    public SigningJob getSigningJob() {
        return signingJob;
    }

    public boolean haveSignatures() {
        return !documentReports.isEmpty();
    }

    public boolean haveInvalidSignatures() {
        return documentReports.stream().anyMatch(DocumentReport::haveInvalidSignatures);
    }

    public boolean isMultiDocumentJob() {
        return signingJob != null && signingJob.getDocumentsForContentChecks().size() > 1;
    }

    public boolean hasMultipleContainerDocuments() {
        return documentReports.stream().anyMatch(DocumentReport::hasMultipleContainerDocuments);
    }

    public boolean shouldShowDocumentContext() {
        return isMultiDocumentJob() || hasMultipleContainerDocuments();
    }

    public int getTotalSignatureCount() {
        return documentReports.stream().mapToInt(DocumentReport::getSignatureCount).sum();
    }

    public List<SignatureEntry> getSignatures() {
        return documentReports.stream()
                .flatMap(documentReport -> documentReport.reports().getSimpleReport().getSignatureIdList().stream()
                        .map(signatureId -> new SignatureEntry(documentReport, signatureId)))
                .toList();
    }

    public List<SignatureEntry> getSignaturesForPreviewDocument(DSSDocument previewDocument, int previewIndex) {
        if (documentReports.size() != 1)
            return documentReports.stream()
                    .filter(documentReport -> documentReport.documentIndex() == previewIndex)
                    .flatMap(documentReport -> signaturesFor(documentReport).stream())
                    .toList();

        var documentReport = documentReports.get(0);
        if (!documentReport.hasMultipleContainerDocuments())
            return signaturesFor(documentReport);

        var previewName = previewDocument.getName();
        return signaturesFor(documentReport).stream()
                .filter(signature -> {
                    var scope = documentReport.getSignatureScopeDocumentNames(signature.signatureId());
                    return scope.isEmpty()
                            || documentReport.signatureCoversAllDocuments(signature.signatureId())
                            || scope.contains(previewName);
                })
                .toList();
    }

    public boolean hasIncompleteContainerCoverage() {
        return documentReports.stream()
                .filter(DocumentReport::hasMultipleContainerDocuments)
                .anyMatch(documentReport -> signaturesFor(documentReport).stream()
                        .anyMatch(signature -> !documentReport.signatureCoversAllDocuments(signature.signatureId())));
    }

    private static List<SignatureEntry> signaturesFor(DocumentReport documentReport) {
        return documentReport.reports().getSimpleReport().getSignatureIdList().stream()
                .map(signatureId -> new SignatureEntry(documentReport, signatureId))
                .toList();
    }
}
