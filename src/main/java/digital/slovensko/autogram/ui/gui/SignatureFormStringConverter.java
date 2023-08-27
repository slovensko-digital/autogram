package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.enumerations.SignatureForm;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.Optional;

public class SignatureFormStringConverter extends StringConverter<SignatureForm> {
    @Override
    public String toString(SignatureForm signatureForm) {
        if (signatureForm == null) {
            return null;
        }
        return signatureForm.toString();
    }

    @Override
    public SignatureForm fromString(String signatureFormString) {
        if (signatureFormString == null) {
            return null;
        }
        Optional<SignatureForm> signatureForm = Arrays.
                stream(SignatureForm.values())
                .filter(sf -> sf.toString().equals(signatureFormString))
                .findFirst();
        return signatureForm.isEmpty() ? null : signatureForm.get();
    }
}
