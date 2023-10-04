package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.util.StringConverter;


public class SignatureLevelStringConverter extends StringConverter<SignatureLevel> {

    public static final String ASIC_XADES = "XAdES ASiC-E";
    public static final String ASIC_CADES = "CAdES ASiC-E";
    public static final String PADES = "PAdES";
    @Override
    public String toString(SignatureLevel signatureLevel) {
        return switch (signatureLevel) {
            case XAdES_BASELINE_B -> ASIC_XADES;
            case PAdES_BASELINE_B -> PADES;
            case CAdES_BASELINE_B -> ASIC_CADES;
            default -> "";
        };
    }

    @Override
    public SignatureLevel fromString(String signatureLevel) {
        return switch (signatureLevel) {
            case ASIC_XADES -> SignatureLevel.XAdES_BASELINE_B;
            case PADES -> SignatureLevel.PAdES_BASELINE_B;
            case ASIC_CADES -> SignatureLevel.CAdES_BASELINE_B;
            default -> null;
        };
    }
}
