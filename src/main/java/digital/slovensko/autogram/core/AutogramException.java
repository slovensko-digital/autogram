package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.DSSException;

public class AutogramException extends RuntimeException {
    private final String heading;
    private final String subheading;
    private final String description;

    public AutogramException(String heading, String subheading, String description, Throwable e) {
        super(e);
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public String getSubheading() {
        return subheading;
    }

    public String getDescription() {
        return description;
    }

    public static AutogramException createFromDSSException(DSSException e) {
        // TODO parsing logic
        return new AutogramException("Nastala nejaka chyba", "Tu podnadpis", "strasne dlha chyba", e);
    }
}
