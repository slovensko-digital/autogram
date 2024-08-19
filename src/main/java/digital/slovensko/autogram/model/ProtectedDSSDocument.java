package digital.slovensko.autogram.model;

import eu.europa.esig.dss.model.DSSDocument;

public interface ProtectedDSSDocument extends DSSDocument {
    char[] getPassword();
    void setPassword(char[] password);

    default boolean isProtected() {
        return getPassword().length != 0;
    }
}
