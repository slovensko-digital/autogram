package digital.slovensko.autogram.core.errors;

import eu.europa.esig.dss.model.DSSException;

public class SlotIndexOutOfRangeException extends AutogramException {
    public SlotIndexOutOfRangeException(DSSException e) {
        super(e);
    }

}
