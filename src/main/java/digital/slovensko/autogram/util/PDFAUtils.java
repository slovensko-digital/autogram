package digital.slovensko.autogram.util;

import eu.europa.esig.dss.model.CommonDocument;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraPDFFoundry;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.InputStream;

public class PDFAUtils {
    private static final VeraPDFFoundry FOUNDRY;

    static {
        VeraGreenfieldFoundryProvider.initialise();
        FOUNDRY = Foundries.defaultInstance();
    }

    public static boolean isCompliant(CommonDocument doc) {
        try (InputStream is = doc.openStream(); PDFAParser parser = FOUNDRY.createParser(is);
             PDFAValidator validator = FOUNDRY.createValidator(PDFAFlavour.PDFA_1_A, false)) {
            return validator.validate(parser).isCompliant();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
