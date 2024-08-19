package digital.slovensko.autogram.util;

import digital.slovensko.autogram.model.ProtectedDSSDocument;
import eu.europa.esig.dss.asic.cades.validation.ASiCContainerWithCAdESValidatorFactory;
import eu.europa.esig.dss.asic.xades.validation.ASiCContainerWithXAdESValidatorFactory;
import eu.europa.esig.dss.cades.validation.CMSDocumentValidatorFactory;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidatorFactory;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidatorFactory;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class DSSUtils {
    public static String parseCN(String rfc2253) {
        try {
            var ldapName = new LdapName(rfc2253);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return rdn.getValue().toString();
                }
            }
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String buildTooltipLabel(DSSPrivateKeyEntry key) {
        var out = "";
        out += key.getCertificate().getSubject().getPrincipal().toString();
        if (key.getCertificate().getIssuer() != null) {
            out += "\n\n" + key.getCertificate().getIssuer().getPrincipal().toString();
        }
        return out;
    }

    public static SignedDocumentValidator createDocumentValidator(ProtectedDSSDocument document) {
        if (new PDFDocumentValidatorFactory().isSupported(document)) {
            var validator = new PDFDocumentValidator(document);
            validator.setPasswordProtection(document.getPassword());
            return validator;
        }

        if (new XMLDocumentValidatorFactory().isSupported(document))
            return new XMLDocumentValidatorFactory().create(document);

        if (new ASiCContainerWithXAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithXAdESValidatorFactory().create(document);

        if (new ASiCContainerWithCAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithCAdESValidatorFactory().create(document);

        if (new CMSDocumentValidatorFactory().isSupported(document))
            return new CMSDocumentValidatorFactory().create(document);

        return null;
    }

    public static String getXdcfFilename(String filename) {
        if (filename == null)
            return "document.xdcf";

        if (filename.endsWith(".xml"))
            return filename.replace(".xml", ".xdcf");

        if (!filename.contains(".xdcf"))
            return filename + ".xdcf";

        return filename;
    }
}
