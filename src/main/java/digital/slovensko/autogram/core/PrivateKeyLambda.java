package digital.slovensko.autogram.core;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

public interface PrivateKeyLambda {
    public void call(DSSPrivateKeyEntry privateKey);
}
