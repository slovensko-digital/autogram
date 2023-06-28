package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class TransformationException extends AutogramException {
    public TransformationException(String message, Exception e) {
        super("Error parsing transformation", "Transition failed with following message:", message, e);
    }
}
