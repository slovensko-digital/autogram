package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.model.DSSDocument;

public interface XdcTransformer {
    DSSDocument transform(DSSDocument dssDocument) throws AutogramException;
}
