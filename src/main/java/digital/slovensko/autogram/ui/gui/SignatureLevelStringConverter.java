package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.util.StringConverter;


public class SignatureLevelStringConverter extends StringConverter<SignatureLevel> {

    public static final String ASIC_XADES = "ASiC XAdES";
    public static final String PADES = "PAdES";
    @Override
    public String toString(SignatureLevel signatureLevel) {
        if (SignatureLevel.XAdES_BASELINE_B == signatureLevel) {
            return ASIC_XADES;
        } else if (SignatureLevel.PAdES_BASELINE_B == signatureLevel) {
            return PADES;
        }
        return "";
    }

    @Override
    public SignatureLevel fromString(String signatureLevel) {
        if (ASIC_XADES.equals(signatureLevel)) {
            return SignatureLevel.XAdES_BASELINE_B;
        } else if (PADES.equals(signatureLevel)) {
            return SignatureLevel.PAdES_BASELINE_B;
        }
        return null;
    }
}
