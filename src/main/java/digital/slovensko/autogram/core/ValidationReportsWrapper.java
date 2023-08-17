package digital.slovensko.autogram.core;

import eu.europa.esig.dss.validation.reports.Reports;

public class ValidationReportsWrapper {
    private final Reports reports;
    private final SigningJob signingJob;

    public ValidationReportsWrapper(Reports reports, SigningJob signingJob) {
        this.reports = reports;
        this.signingJob = signingJob;
    }

    public Reports getReports() {
        return reports;
    }

    public SigningJob getSigningJob() {
        return signingJob;
    }
}
